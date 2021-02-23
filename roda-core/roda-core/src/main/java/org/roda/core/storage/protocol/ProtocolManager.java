package org.roda.core.storage.protocol;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ProtocolManager {
  InputStream getInputStream();
  Boolean isAvailable();
  void downloadResource(Path target);
}
