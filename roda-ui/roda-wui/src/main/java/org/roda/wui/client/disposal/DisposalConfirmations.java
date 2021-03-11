/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal;

import java.util.List;

import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalConfirmationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.DisposalConfirmationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.disposal.confirmations.CreateDisposalConfirmation;
import org.roda.wui.client.disposal.confirmations.ShowDisposalConfirmation;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmations extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "confirmations";
    }
  };

  private static DisposalConfirmations instance = null;

  interface MyUiBinder extends UiBinder<Widget, DisposalConfirmations> {
  }

  private static DisposalConfirmations.MyUiBinder uiBinder = GWT.create(DisposalConfirmations.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel disposalConfirmationDescription;

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  @UiField
  SimplePanel actionsSidebar;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  /**
   * Create a disposal confirmation page
   */
  public DisposalConfirmations() {
    ListBuilder<DisposalConfirmation> disposalConfirmationListBuilder = new ListBuilder<>(
      () -> new DisposalConfirmationList(),
      new AsyncTableCellOptions<>(DisposalConfirmation.class, "Disposal_confirmations").bindOpener()
        .withAutoUpdate(5000));

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(disposalConfirmationListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    final DisposalConfirmationActions confirmationActions = DisposalConfirmationActions.get();
    SidebarUtils.toggleSidebar(contentFlowPanel, sidebarFlowPanel, confirmationActions.hasAnyRoles());
    actionsSidebar.setWidget(new ActionableWidgetBuilder<>(confirmationActions)
      .buildListWithObjects(new ActionableObject<>(DisposalConfirmation.class)));

    disposalConfirmationDescription.add(new HTMLWidgetWrapper("DisposalConfirmationDescription.html"));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static DisposalConfirmations getInstance() {
    if (instance == null) {
      instance = new DisposalConfirmations();
    } else {
      instance.refresh();
    }
    return instance;
  }

  private void refresh() {
    searchWrapper.refreshCurrentList();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else {
      String basePage = historyTokens.remove(0);
      if (ShowDisposalConfirmation.RESOLVER.getHistoryToken().equals(basePage)) {
        ShowDisposalConfirmation.RESOLVER.resolve(historyTokens, callback);
      } else if (CreateDisposalConfirmation.RESOLVER.getHistoryToken().equals(basePage)) {
        CreateDisposalConfirmation.RESOLVER.resolve(historyTokens, callback);
      }
    }
  }
}
