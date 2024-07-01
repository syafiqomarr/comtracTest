package com.ssm.ezbiz.comtrac;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.hibernate.Hibernate;

import com.ssm.ezbiz.robformA.EditRobFormAPage;
import com.ssm.ezbiz.robformA.ViewRobFormAPage2;
import com.ssm.ezbiz.service.RobFormAService;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.db.SearchCriteria;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.odt.LLPOdtUtils;
import com.ssm.llp.base.page.SSMPagingNavigator;
import com.ssm.llp.base.page.SecBasePanel;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSortableDataProvider;
import com.ssm.llp.base.wicket.SSMDownloadLink;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.ezbiz.model.RobFormA;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;

@SuppressWarnings({ "all" })
public class ComtractListStatus extends SecBasePanel {

	@SpringBean(name = "RobTrainingConfigService")
	private RobTrainingConfigService robTrainingConfigService;

	@SpringBean(name = "RobTrainingTransactionService")
	private RobTrainingTransactionService robTrainingTransactionService;

	public ComtractListStatus(String id, String tabStatus) {
		super(id);
		init(id, new String[] { tabStatus });
	}

	public ComtractListStatus(String id, String tabStatusArr[]) {
		super(id);
		init(id, tabStatusArr);
	}

	public void init(String id, String tabStatusArr[]) {

		SearchCriteria sc = new SearchCriteria("createBy", SearchCriteria.EQUAL, UserEnvironmentHelper.getLoginName());
		sc = sc.andIfInNotNull("status", tabStatusArr, false);

		SSMSortableDataProvider dp = new SSMSortableDataProvider("updateDt", SortOrder.DESCENDING, sc,
				RobTrainingTransactionService.class);

		final SSMDataView<RobTrainingTransaction> dataView = new SSMDataView<RobTrainingTransaction>("sorting", dp) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<RobTrainingTransaction> item) {
				final RobTrainingTransaction robTrainingTransaction = item.getModelObject();

				item.add(new SSMLabel("transactionCode", robTrainingTransaction.getTransactionCode()));
				item.add(new SSMLabel("trainingCode", robTrainingTransaction.getTrainingId().getTrainingCode()));
				item.add(new SSMLabel("trainingName", robTrainingTransaction.getTrainingId().getTrainingName()));
				item.add(new SSMLabel("status", robTrainingTransaction.getStatus()));
				item.add(new SSMLabel("updateDt", robTrainingTransaction.getUpdateDt(), "dd/MM/yyyy hh:mm:ss a"));

				item.add(new Link("detail", item.getDefaultModel()) {
					public void onClick() {
						RobTrainingTransaction robTrainingTransaction = item.getModelObject();
						if (Parameter.COMTRAC_TRANSACTION_STATUS_data_entry
								.equals(robTrainingTransaction.getStatus())) {

							if (Parameter.COMTRAC_PAYMENT_CHANNEL_ezbiz
									.equals(robTrainingTransaction.getPaymentChannel())
									&& Parameter.COMTRAC_REGISTRATION_CHANNEL_ezbiz
											.equals(robTrainingTransaction.getRegistrationChannel())) {

								setResponsePage(new SelectComtracTraining(robTrainingTransaction.getTransactionCode()));

							} else if (Parameter.COMTRAC_PAYMENT_CHANNEL_unpaid
									.equals(robTrainingTransaction.getPaymentChannel())
									&& Parameter.COMTRAC_REGISTRATION_CHANNEL_ecomtrac
											.equals(robTrainingTransaction.getRegistrationChannel())) {

								setResponsePage(new SelectLPOPayment(robTrainingTransaction.getTransactionCode()));

							} else if (Parameter.COMTRAC_PAYMENT_CHANNEL_ezbiz
									.equals(robTrainingTransaction.getPaymentChannel())
									&& Parameter.COMTRAC_REGISTRATION_CHANNEL_ecomtrac
											.equals(robTrainingTransaction.getRegistrationChannel())) {

								setResponsePage(
										new SelectCorporateTraining(robTrainingTransaction.getTransactionCode()));

							}
						} else {

							setResponsePage(new ViewListParticipantSummary(robTrainingTransaction.getTransactionCode(),
									getPage()));

						}
					}
				});

				SSMDownloadLink printConfirmation = new SSMDownloadLink("printConfirmation") {
					public void onClick() {
						RobTrainingTransaction transaction = robTrainingTransactionService
								.findByTransactionCodeWithParticipant(robTrainingTransaction.getTransactionCode());
						Map mapData = new HashMap();
						mapData.put("robTrainingTransaction", transaction);
						mapData.put("participantList", transaction.getRobTrainingParticipantList());

						byte bytePdfContent[] = LLPOdtUtils.generatePdf("COMTRAC_CONFIRMATION_SLIP", mapData);
						generateDownload(robTrainingTransaction.getTransactionCode() + " - Confirmation Slip.pdf",
								bytePdfContent);
					}
				};
				printConfirmation.setOutputMarkupId(true);
				item.add(printConfirmation);

				if (Parameter.COMTRAC_TRANSACTION_STATUS_payment_success.equals(robTrainingTransaction.getStatus())) {
					printConfirmation.setVisible(true);
				} else {
					printConfirmation.setVisible(false);
				}

				item.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}
				}));
			}
		};

		dataView.setItemsPerPage(20L);

		add(dataView);
		add(new SSMPagingNavigator("navigator", dataView));
		add(new NavigatorLabel("navigatorLabel", dataView));

	}

	public void generateDownload(String fileName, final byte[] byteData) {

		/* System.out.println("file Name :::::::::: " + fileName); */
		// System.out.println("byte length "+new String(byteData, 0));

		AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
			@Override
			public void write(OutputStream output) throws IOException {
				output.write(byteData);
				output.flush();
			}
		};

		/* System.out.println("AFTER WRITE OUTPUTSTREAM —---------"); */
		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, fileName);
		getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
	}

	public SearchCriteria getSearchCriteria() {
		SearchCriteria sc = new SearchCriteria("createBy", SearchCriteria.EQUAL, UserEnvironmentHelper.getLoginName());
		return sc;
	}

}
