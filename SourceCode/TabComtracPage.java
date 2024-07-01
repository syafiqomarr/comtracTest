package com.ssm.ezbiz.comtrac;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.ssm.ezbiz.robformA.ListRobFormAPage;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMLabel;

@SuppressWarnings({ "all" })
public class TabComtracPage extends SecBasePage {

	public TabComtracPage() {
		this(null, null);
	}

	public TabComtracPage(String alert, AjaxRequestTarget target) {

		setOutputMarkupId(true);
		ComtractListStatus contracListDraftPanel = new ComtractListStatus("draftPanel",
				new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_data_entry });
		contracListDraftPanel.setOutputMarkupId(true);
		add(contracListDraftPanel);

		final Label notyJs = new Label("notyJs", Model.of(""));
		notyJs.setEscapeModelStrings(false);
		notyJs.setOutputMarkupId(true);
		notyJs.setOutputMarkupPlaceholderTag(true);
		add(notyJs);

		if (alert != null) {
			notyJs.setDefaultModelObject(alert);
			target.add(notyJs);
		}

		SSMLabel pendingPaymentPanel = new SSMLabel("pendingPaymentPanel");
		pendingPaymentPanel.setOutputMarkupId(true);
		add(pendingPaymentPanel);
		
		SSMLabel inProcessPanel = new SSMLabel("inProcessPanel");
		inProcessPanel.setOutputMarkupId(true);
		add(inProcessPanel);

		SSMLabel paymentSuccessPanel = new SSMLabel("paymentSuccessPanel");
		paymentSuccessPanel.setOutputMarkupId(true);
		add(paymentSuccessPanel);

		SSMLabel cancelPanel = new SSMLabel("cancelPanel");
		cancelPanel.setOutputMarkupId(true);
		add(cancelPanel);
		
		SSMLabel rejectPanel = new SSMLabel("rejectPanel");
		rejectPanel.setOutputMarkupId(true);
		add(rejectPanel);

		SSMAjaxLink draftLink = new SSMAjaxLink<Void>("draftLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ComtractListStatus panel = new ComtractListStatus("draftPanel",
						new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_data_entry });
				panel.setOutputMarkupId(true);
				getPage().replace(panel);
				target.add(panel);
			}
		};
		add(draftLink);

		SSMAjaxLink pendingPaymentLink = new SSMAjaxLink<Void>("pendingPaymentLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ComtractListStatus panel = new ComtractListStatus("pendingPaymentPanel",
						new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_pending_payment });
				panel.setOutputMarkupId(true);
				getPage().replace(panel);
				target.add(panel);
			}
		};
		add(pendingPaymentLink);
		
		SSMAjaxLink inProcessLink = new SSMAjaxLink<Void>("inProcessLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ComtractListStatus panel = new ComtractListStatus("inProcessPanel",
						new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_in_process });
				panel.setOutputMarkupId(true);
				getPage().replace(panel);
				target.add(panel);
			}
		};
		add(inProcessLink);

		SSMAjaxLink paymentSuccessLink = new SSMAjaxLink<Void>("paymentSuccessLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ComtractListStatus panel = new ComtractListStatus("paymentSuccessPanel",
						new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_payment_success });
				panel.setOutputMarkupId(true);
				getPage().replace(panel);
				target.add(panel);
			}
		};
		add(paymentSuccessLink);

		SSMAjaxLink cancelLink = new SSMAjaxLink<Void>("cancelLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ComtractListStatus panel = new ComtractListStatus("cancelPanel",
						new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_cancel });
				panel.setOutputMarkupId(true);
				getPage().replace(panel);
				target.add(panel);
			}
		};
		add(cancelLink);
		
		SSMAjaxLink rejectLink = new SSMAjaxLink<Void>("rejectLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ComtractListStatus panel = new ComtractListStatus("rejectPanel",
						new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_reject });
				panel.setOutputMarkupId(true);
				getPage().replace(panel);
				target.add(panel);
			}
		};
		add(rejectLink);

		if (UserEnvironmentHelper.isInternalUser()) {
			draftLink.setVisible(true);
			pendingPaymentLink.setVisible(true);
			paymentSuccessLink.setVisible(true);
			cancelLink.setVisible(true);
			inProcessLink.setVisible(false);
			rejectLink.setVisible(false);
		} else {
			draftLink.setVisible(true);
			pendingPaymentLink.setVisible(true);
			paymentSuccessLink.setVisible(true);
			cancelLink.setVisible(true);
			inProcessLink.setVisible(true);
			rejectLink.setVisible(true);
		}
	}

	@Override
	public String getPageTitle() {
		// TODO Auto-generated method stub
		return "menu.myBiz.tabComtracPage";
	}
}
