package org.roda.core.plugins.plugins.internal.disposal.confirmation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata;
import org.roda.core.index.IndexService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.rsync.RsyncUtils;
import org.roda.core.util.CommandException;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationPluginUtils {

  public static void copyAIPToDisposalBin(AIP aip, String disposalConfirmationId, List<String> rsyncOptions)
    throws RequestNotValidException, GenericException, CommandException {
    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aip.getId());
    Path aipPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), aipStoragePath);

    // disposal-bin/<disposalConfirmationId>/aip/<aipId>
    Path disposalBinPath = RodaCoreFactory.getDisposalBinDirectoryPath().resolve(disposalConfirmationId)
      .resolve(RodaConstants.CORE_AIP_FOLDER).resolve(aipStoragePath.getName());

    RsyncUtils.executeRsync(aipPath, disposalBinPath, rsyncOptions);
  }

  public static void copyAIPFromDisposalBin(String aipId, String disposalConfirmationId, List<String> rsyncOptions)
    throws RequestNotValidException, GenericException, CommandException {
    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aipId);
    Path aipPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), aipStoragePath);

    // disposal-bin/<disposalConfirmationId>/aip/<aipId>
    Path disposalBinPath = RodaCoreFactory.getDisposalBinDirectoryPath().resolve(disposalConfirmationId)
      .resolve(RodaConstants.CORE_AIP_FOLDER).resolve(aipStoragePath.getName());

    RsyncUtils.executeRsync(disposalBinPath, aipPath, rsyncOptions);
  }

  public static DisposalConfirmation getDisposalConfirmation(String confirmationId, String title, long storageSize,
    Set<String> disposalHolds, Set<String> disposalSchedules, long numberOfAIPs, Map<String, String> extraFields) {

    DisposalConfirmation confirmationMetadata = new DisposalConfirmation();
    confirmationMetadata.setId(confirmationId);
    confirmationMetadata.setTitle(title);
    confirmationMetadata.setSize(storageSize);
    confirmationMetadata.addDisposalHoldIds(disposalHolds);
    confirmationMetadata.addDisposalScheduleIds(disposalSchedules);
    confirmationMetadata.setNumberOfAIPs(numberOfAIPs);
    confirmationMetadata.setExtraFields(extraFields);

    return confirmationMetadata;
  }

  public static DisposalConfirmationAIPEntry getAIPEntryFromAIP(IndexService indexService, AIP aip,
    Set<String> disposalSchedules, Set<String> disposalHolds)
    throws GenericException, RequestNotValidException, NotFoundException {
    DisposalConfirmationAIPEntry entry = new DisposalConfirmationAIPEntry();

    IndexedAIP indexedAIP = indexService.retrieve(IndexedAIP.class, aip.getId(),
      Arrays.asList(RodaConstants.AIP_LEVEL, RodaConstants.AIP_TITLE, RodaConstants.AIP_OVERDUE_DATE));

    entry.setAipId(aip.getId());
    entry.setAipLevel(indexedAIP.getLevel());
    entry.setAipTitle(indexedAIP.getTitle());
    entry.setAipCreationDate(aip.getCreatedOn());
    entry.setAipOverdueDate(indexedAIP.getOverdueDate());

    entry.setAipDisposalScheduleId(aip.getDisposalScheduleId());
    disposalSchedules.add(aip.getDisposalScheduleId());

    entry.setAipDisposalHoldIds(getDisposalHoldIds(aip.getDisposal().getHolds()));
    disposalHolds.addAll(entry.getAipDisposalHoldIds());

    getStorageSizeInBytesForAIP(indexService, aip.getId(), entry);

    return entry;
  }

  private static List<String> getDisposalHoldIds(List<DisposalHoldAIPMetadata> disposalHoldAssociation) {
    List<String> holdIds = new ArrayList<>();

    for (DisposalHoldAIPMetadata holdAssociation : disposalHoldAssociation) {
      holdIds.add(holdAssociation.getId());
    }

    return holdIds;
  }

  private static void getStorageSizeInBytesForAIP(IndexService indexService, String aipId,
    DisposalConfirmationAIPEntry entry) throws GenericException, RequestNotValidException {

    long totalSize = 0L;
    long totalOfDataFiles = 0L;

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));

    IndexResult<IndexedRepresentation> indexedRepresentationIndexResult = indexService.find(IndexedRepresentation.class,
      filter, null, new Sublist(0, RodaConstants.DEFAULT_PAGINATION_VALUE),
      Arrays.asList(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES));

    for (IndexedRepresentation representation : indexedRepresentationIndexResult.getResults()) {
      totalSize += representation.getSizeInBytes();
      totalOfDataFiles += representation.getNumberOfDataFiles();
    }

    entry.setAipSize(totalSize);
    entry.setAipNumberOfFiles(totalOfDataFiles);
  }
}
