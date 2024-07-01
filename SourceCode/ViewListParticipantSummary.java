package com.ssm.ezbiz.comtrac;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import com.ssm.base.common.util.DateUtil;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.model.LlpFileData;
import com.ssm.llp.base.common.model.LlpPaymentTransactionDetail;
import com.ssm.llp.base.common.model.LlpEmailLog;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.common.service.LlpFileDataService;
import com.ssm.llp.base.common.service.LlpParametersService;
import com.ssm.llp.base.common.service.LlpPaymentReceiptService;
import com.ssm.llp.base.common.service.LlpPaymentTransactionService;
import com.ssm.llp.base.common.service.MailService;
import com.ssm.llp.base.odt.LLPOdtUtils;
import com.ssm.llp.base.page.PaymentDetailPage;
import com.ssm.llp.base.page.SSMPagingNavigator;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSessionSortableDataProvider;
import com.ssm.llp.base.wicket.SSMDownloadLink;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMDropDownChoice;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextArea;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.mod1.service.LlpUserProfileService;

@SuppressWarnings({ "all" })
public class ViewListParticipantSummary extends SecBasePage {

	@SpringBean(name = "RobTrainingParticipantService")
	private RobTrainingParticipantService robTrainingParticipantService;

	@SpringBean(name = "RobTrainingTransactionService")
	private RobTrainingTransactionService robTrainingTransactionService;

	@SpringBean(name = "RobTrainingConfigService")
	private RobTrainingConfigService robTrainingConfigService;

	@SpringBean(name = "LlpPaymentTransactionService")
	private LlpPaymentTransactionService llpPaymentTransactionService;

	@SpringBean(name = "LlpPaymentReceiptService")
	private LlpPaymentReceiptService llpPaymentReceiptService;

	@SpringBean(name = "LlpParametersService")
	private LlpParametersService llpParametersService;

	@SpringBean(name = "MailService")
	MailService mailService;

	@SpringBean(name = "LlpUserProfileService")
	LlpUserProfileService llpUserProfileService;

	@SpringBean(name = "LlpFileDataService")
	LlpFileDataService llpFileDataService;

	List<RobTrainingParticipant> listParticipant;

	public ViewListParticipantSummary(String transactionCode, final Page fromPage) {

		final RobTrainingTransaction robTrainingTransaction = robTrainingTransactionService
				.findByTransactionCodeWithParticipant(transactionCode);

		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				return robTrainingTransaction;
			}
		}));

		add(new ParticipanSummaryForm("form", getDefaultModel(), fromPage));
	}

	public class ParticipanSummaryForm extends Form implements Serializable {

		public ParticipanSummaryForm(String id, IModel m, final Page fromPage) {
			super(id, m);

			final RobTrainingTransaction robTrainingTransaction = (RobTrainingTransaction) m.getObject();
			RobTrainingConfig robTrainingConfig = robTrainingTransaction.getTrainingId();
			LlpUserProfile userProfile = llpUserProfileService
					.findProfileInfoByUserIdNo(robTrainingTransaction.getLodgerId());
			Button save = null;
			DecimalFormat df = new DecimalFormat("#,###,##0.00");

			final WebMarkupContainer wmcListParticipant = new WebMarkupContainer("wmcListParticipant");
			wmcListParticipant.setOutputMarkupId(true);
			wmcListParticipant.setOutputMarkupPlaceholderTag(true);
			add(wmcListParticipant);

			final ModalWindow addParticipantPopup = new ModalWindow("addParticipantPopup");
			addParticipantPopup.setWidthUnit("px");
			addParticipantPopup.setHeightUnit("px");
			addParticipantPopup.setInitialWidth(850);
			addParticipantPopup.setInitialHeight(650);

			add(addParticipantPopup);

			addParticipantPopup.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
				@Override
				public void onClose(AjaxRequestTarget target) {
					setResponsePage(
							new ViewListParticipantSummary(robTrainingTransaction.getTransactionCode(), fromPage));
				}
			});

			SSMTextField louLoaRefNo = new SSMTextField("louLoaRefNo");
			louLoaRefNo.setOutputMarkupId(true);
			louLoaRefNo.setVisible(false);
			add(louLoaRefNo);

			SSMTextField invoiceNo = new SSMTextField("invoiceNo");
			invoiceNo.setOutputMarkupId(true);
			invoiceNo.setVisible(false);
			add(invoiceNo);

			SSMTextField receiptNo = new SSMTextField("receiptNo");
			receiptNo.setOutputMarkupId(true);
			receiptNo.setVisible(false);
			add(receiptNo);

			SSMTextArea remarks = new SSMTextArea("remarks");
			remarks.setOutputMarkupId(true);
			remarks.setVisible(true);
			add(remarks);

			add(new SSMLabel("transactionCode", robTrainingTransaction.getTransactionCode()));
			add(new SSMLabel("trainingCode", robTrainingTransaction.getTrainingId().getTrainingCode()));
			add(new SSMLabel("trainingName", robTrainingTransaction.getTrainingId().getTrainingName()));
			add(new SSMLabel("lodgerName", robTrainingTransaction.getLodgerName()));
			add(new SSMLabel("lodgerHpNo", userProfile.getHpNo()));
			add(new SSMLabel("lodgerEmail", userProfile.getEmail()));
			add(new SSMLabel("icNo", robTrainingTransaction.getLodgerId()));
			add(new SSMLabel("createDt", robTrainingTransaction.getCreateDt(), "dd/MM/yyyy hh:mm a"));
			add(new SSMLabel("trainingStartTime", robTrainingConfig.getTrainingStartTime()));
			add(new SSMLabel("trainingEndTime", robTrainingConfig.getTrainingEndTime()));
			add(new SSMLabel("cpePoint", robTrainingConfig.getCpePoint()));
			add(new SSMLabel("trainingVenue", robTrainingConfig.getTrainingVenue()));

			String receipt = "-";
			String invoice = "-";
			String lpoloa = "-";

			if (!StringUtils.isBlank(robTrainingTransaction.getLouLoaRefNo())) {
				lpoloa = robTrainingTransaction.getLouLoaRefNo();
			}

			if (!StringUtils.isBlank(robTrainingTransaction.getInvoiceNo())) {
				invoice = robTrainingTransaction.getInvoiceNo();
			}

			if (!StringUtils.isBlank(robTrainingTransaction.getReceiptNo())) {
				receipt = robTrainingTransaction.getReceiptNo();
			}

			SSMLabel louLoaRefNoLabel = new SSMLabel("louLoaRefNoLabel", lpoloa);
			louLoaRefNoLabel.setOutputMarkupId(true);
			louLoaRefNoLabel.setVisible(false);
			add(louLoaRefNoLabel);

			SSMLabel invoiceLabel = new SSMLabel("invoiceLabel", invoice);
			invoiceLabel.setOutputMarkupId(true);
			invoiceLabel.setVisible(false);
			add(invoiceLabel);

			SSMLabel receiptLabel = new SSMLabel("receiptLabel", receipt);
			receiptLabel.setOutputMarkupId(true);
			receiptLabel.setVisible(false);
			add(receiptLabel);

//				SSMDropDownChoice status = new SSMDropDownChoice("status", Parameter.COMTRAC_TRANSACTION_STATUS);
//				status.setOutputMarkupId(true);
//				status.setVisible(true);
//				add(status);

//				SSMDropDownChoice paymentChannel = new SSMDropDownChoice("paymentChannel", Parameter.COMTRAC_PAYMENT_CHANNEL);
//				add(paymentChannel);

			String statusDesc = getCodeTypeWithValue(Parameter.COMTRAC_TRANSACTION_STATUS,
					robTrainingTransaction.getStatus());
			String paymentChannelDesc = getCodeTypeWithValue(Parameter.COMTRAC_PAYMENT_CHANNEL,
					robTrainingTransaction.getPaymentChannel());

			SSMTextField status = new SSMTextField("status", new PropertyModel(statusDesc, ""));
			status.setOutputMarkupId(true);
			status.setVisible(true);
			status.setReadOnly(true);
			add(status);

			SSMTextField paymentChannel = new SSMTextField("paymentChannel", new PropertyModel(paymentChannelDesc, ""));
			paymentChannel.setReadOnly(true);
			add(paymentChannel);

			SSMLabel remarkLabel = new SSMLabel("remarkLabel", robTrainingTransaction.getRemarks());
			remarkLabel.setOutputMarkupId(true);
			remarkLabel.setVisible(false);
			add(remarkLabel);

			save = new Button("save") {
				public void onSubmit() {
					RobTrainingTransaction model = (RobTrainingTransaction) getForm().getModelObject();

					if (Parameter.COMTRAC_TRANSACTION_STATUS_cancel.equals(model.getStatus())) {
						RobTrainingConfig tempObject = robTrainingConfigService
								.findById(model.getTrainingId().getTrainingId());
						Integer currentpax = tempObject.getCurrentPax();
						tempObject.setCurrentPax(currentpax - 1);
						robTrainingConfigService.update(tempObject);
					}
					robTrainingTransactionService.update(model);

					setResponsePage(new ViewListParticipantSummary(model.getTransactionCode(), fromPage));
				}
			};
			add(save);

			if (UserEnvironmentHelper.isInternalUser()) {

				save.setVisible(true);
				louLoaRefNo.setVisible(true);
				/* louLoaRefNoLabel.setVisible(false); */
				receiptNo.setVisible(true);
				/* receiptLabel.setVisible(false); */
				invoiceNo.setVisible(true);
				/* invoiceLabel.setVisible(false); */
				status.setVisible(true);

			} else {

				save.setVisible(false);
				louLoaRefNo.setVisible(false);
				louLoaRefNoLabel.setVisible(true);
				receiptNo.setVisible(false);
				receiptLabel.setVisible(true);
				invoiceNo.setVisible(false);
				invoiceLabel.setVisible(true);
				status.setVisible(false);
			}

			if (Parameter.COMTRAC_TRANSACTION_STATUS_cancel.equals(robTrainingTransaction.getStatus())) {
				save.setVisible(false);
				louLoaRefNo.setVisible(false);
				louLoaRefNoLabel.setVisible(true);
				receiptNo.setVisible(false);
				receiptLabel.setVisible(true);
				invoiceNo.setVisible(false);
				invoiceLabel.setVisible(true);
				if (UserEnvironmentHelper.isInternalUser()) {
					remarkLabel.setVisible(true);
				} else {
					remarkLabel.setVisible(false);
				}

				status.setVisible(false);
			}

			if (!robTrainingTransaction.getLodgerId().equals("SSM STAF")) {
				status.setVisible(false);
			}
			add(new SSMLabel("totalPax", robTrainingTransaction.getTotalPax()));
			add(new SSMLabel("amount", df.format(robTrainingTransaction.getAmount())));

			SSMAjaxLink resendEmail = new SSMAjaxLink("resendEmail") {
				@Override
				public void onClick(AjaxRequestTarget target) {

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					DecimalFormat df = new DecimalFormat("#,###,##0.00");

					String lodgerName = "us";
					String lodgerPhone = "603-7721 4000";

					String trainingDt = sdf.format(robTrainingConfig.getTrainingStartDt());
					if (!robTrainingConfig.getTrainingEndDt().equals(robTrainingConfig.getTrainingStartDt())) {
						trainingDt += " - " + sdf.format(robTrainingConfig.getTrainingEndDt());
					}

					for (RobTrainingParticipant participant : robTrainingTransaction.getRobTrainingParticipantList()) {

						if (!participant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")) {
							LlpUserProfile llpUserProfile = llpUserProfileService
									.findProfileInfoByUserId(participant.getRobTrainingTransaction().getLodgerName());
							lodgerName = llpUserProfile.getName();
							lodgerPhone = llpUserProfile.getHpNo();
						}

						mailService.sendMail(participant.getEmail(), "email.notification.comtrac.confirmation.subject",
								participant.getRobTrainingTransaction().getTransactionCode(),
								"email.notification.comtrac.confirmation.body",
								participant.getRobTrainingTransaction().getTrainingId().getTrainingName(),
								participant.getRobTrainingTransaction().getTrainingId().getTrainingCode(), trainingDt,
								participant.getRobTrainingTransaction().getTrainingId().getTrainingDesc(), lodgerName,
								lodgerPhone);
					}
				}
			};
			resendEmail.setConfirmQuestion("page.comtrac.attendees.email.confirm");
			wmcListParticipant.add(resendEmail);

			SSMSessionSortableDataProvider dpTraining = new SSMSessionSortableDataProvider("",
					robTrainingTransaction.getRobTrainingParticipantList());
			SSMDataView<RobTrainingParticipant> dataView = new SSMDataView<RobTrainingParticipant>(
					"sortingRobParticipantTransaction", dpTraining) {

				@Override
				protected void populateItem(final Item<RobTrainingParticipant> item) {

					final RobTrainingParticipant robTrainingParticipant = item.getModelObject();

					item.add(new SSMLabel("index", item.getIndex() + 1));
					item.add(new SSMLabel("name", robTrainingParticipant.getName()));
					item.add(new SSMLabel("icNo", robTrainingParticipant.getIcNo()));
					item.add(new SSMLabel("telNo", robTrainingParticipant.getTelNo()));
					item.add(new SSMLabel("email", robTrainingParticipant.getEmail()));
					item.add(new SSMLabel("amount", robTrainingParticipant.getAmount()));

					item.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>() {
						private static final long serialVersionUID = 1L;

						@Override
						public String getObject() {
							return (item.getIndex() % 2 == 1) ? "even" : "odd";
						}
					}));

					AjaxLink viewInfo = new AjaxLink("viewInfo", item.getDefaultModel()) {
						public void onClick(AjaxRequestTarget target) {

							addParticipantPopup.setPageCreator(new ModalWindow.PageCreator() {
								@Override
								public Page createPage() {
									if (UserEnvironmentHelper.isInternalUser()
											&& Parameter.COMTRAC_TRANSACTION_STATUS_payment_success
													.equals(robTrainingTransaction.getStatus())) {
										return new AddTrainingParticipantForm(robTrainingParticipant, robTrainingConfig,
												addParticipantPopup, item.getIndex(), true, getPage());// edit record
									} else {
										return new ViewAttendeesInfo(robTrainingParticipant, addParticipantPopup);// edit
																													// record
									}
								}
							});
							addParticipantPopup.show(target);
						}
					};
					item.add(viewInfo);

					SSMAjaxLink delete = new SSMAjaxLink("delete") {
						public void onClick(AjaxRequestTarget target) {
							RobTrainingParticipant robTrainingParticipant = item.getModelObject();

							Double tempAmount = robTrainingParticipant.getAmount();
							Double tempGstAmount = robTrainingParticipant.getGstAmount();

							// delete participant
							robTrainingParticipantService
									.deleteUsingParticipantId(robTrainingParticipant.getParticipantId());

							// update data in transaction
							robTrainingTransaction.setAmount(robTrainingTransaction.getAmount() - tempAmount);
							robTrainingTransaction.setGstAmount(robTrainingTransaction.getGstAmount() - tempGstAmount);
							robTrainingTransaction.setTotalPax(robTrainingTransaction.getTotalPax() - 1);
							robTrainingTransactionService.update(robTrainingTransaction);

							// update data in training config
							RobTrainingConfig config = robTrainingConfigService
									.findById(robTrainingTransaction.getTrainingId().getTrainingId());
							config.setCurrentPax(config.getCurrentPax() - 1);
							robTrainingConfigService.update(config);

							setResponsePage(new ViewListParticipantSummary(robTrainingTransaction.getTransactionCode(),
									fromPage));
						}
					};
					delete.setConfirmQuestion("participant.confirmDelete");
					delete.setOutputMarkupId(true);
					item.add(delete);

					SSMDownloadLink downloadCert = new SSMDownloadLink("downloadCert") {
						public void onClick() {
							RobTrainingParticipant robTrainingParticipant = item.getModelObject();
							LlpFileData fileData = llpFileDataService.findById(robTrainingParticipant.getFileId());

//							RobTrainingTransaction transaction = robTrainingTransactionService.findByTransactionCodeWithParticipant(robTrainingTransaction.getTransactionCode());
//							Map mapData = new HashMap();
//							mapData.put("robTrainingTransaction", transaction);
//							mapData.put("participantList", transaction.getRobTrainingParticipantList());
//							
//							byte bytePdfContent[] = LLPOdtUtils.generatePdf("COMTRAC_CONFIRMATION_SLIP", mapData);
							generateDownload(robTrainingParticipant.getIcNo() + "_CERTIFICATE.pdf",
									fileData.getFileData());

						}
					};
					downloadCert.setOutputMarkupId(true);
					item.add(downloadCert);

					final Calendar c30DayAfter = Calendar.getInstance();
					c30DayAfter.setTime(robTrainingParticipant.getUpdateDt());
					c30DayAfter.add(Calendar.DATE, +31);

					// hide downloadCert for public after 30 days from participant update date
					if (robTrainingParticipant.getFileId() == null) {
						downloadCert.setVisible(false);
					} else {
						if (c30DayAfter.getTime().after(DateUtil.getCurrentDate())) {
							downloadCert.setVisible(true);
						} else {
							downloadCert.setVisible(false);
						}
					}

					if (UserEnvironmentHelper.isInternalUser()
							&& Parameter.COMTRAC_TRANSACTION_STATUS_payment_success
									.equals(robTrainingTransaction.getStatus())
							&& robTrainingParticipant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")) {
						delete.setVisible(true);
					} else {
						delete.setVisible(false);
					}

				}

			};

			SSMAjaxLink submitPayment = new SSMAjaxLink("submitPayment") {
				@Override
				public void onClick(AjaxRequestTarget target) {

					List<LlpPaymentTransactionDetail> listPaymentItems = new ArrayList<LlpPaymentTransactionDetail>();

					for (RobTrainingParticipant participant : robTrainingTransaction.getRobTrainingParticipantList()) {
						LlpPaymentTransactionDetail paymentItem = new LlpPaymentTransactionDetail();
						if (Parameter.COMTRAC_FEE_TYPE_standard.equals(participant.getFeeType())) {
							paymentItem.setPaymentItem(robTrainingTransaction.getTrainingId().getTrainingCode() + "_"
									+ Parameter.COMTRAC_FEE_TYPE_standard);
						} else {
							paymentItem.setPaymentItem(robTrainingTransaction.getTrainingId().getTrainingCode() + "_"
									+ Parameter.COMTRAC_FEE_TYPE_special);
						}
						paymentItem.setQuantity(1);
						paymentItem.setPaymentDet(participant.getName() + " (" + participant.getIcNo() + ")");
						paymentItem.setAmount(participant.getAmount());
						paymentItem.setGstAmt(participant.getGstAmount());
						paymentItem.setGstCode(participant.getGstCode());
						listPaymentItems.add(paymentItem);
					}

					setResponsePage(new PaymentDetailPage(robTrainingTransaction.getTransactionCode(),
							RobTrainingTransactionService.class.getSimpleName(), robTrainingTransaction,
							listPaymentItems));

				}
			};
			submitPayment.setOutputMarkupId(true);
			add(submitPayment);

			if (Parameter.COMTRAC_TRANSACTION_STATUS_pending_payment.equals(robTrainingTransaction.getStatus())) {
				submitPayment.setVisible(true);
			} else {
				submitPayment.setVisible(false);
			}

			SSMDownloadLink printConfirmation = new SSMDownloadLink("printConfirmation") {
				public void onClick() {

					RobTrainingTransaction trainingTransaction = robTrainingTransactionService
							.findByTransactionCodeWithParticipant(robTrainingTransaction.getTransactionCode());
					Map mapData = new HashMap();
					mapData.put("robTrainingTransaction", trainingTransaction);
					mapData.put("participantList", trainingTransaction.getRobTrainingParticipantList());

//					File file = new File("C:/Users/nhasrizal/Downloads/COMTRAC_CONFIRMATION_SLIP.odt");
					byte bytePdfContent[] = LLPOdtUtils.generatePdf("COMTRAC_CONFIRMATION_SLIP", mapData);
					generateDownload(robTrainingTransaction.getTransactionCode() + " - Confirmation Slip.pdf",
							bytePdfContent);
				}
			};
			printConfirmation.setOutputMarkupId(true);
			add(printConfirmation);

			// Download Supp Doc
			SSMDownloadLink downloadSupportingDoc = new SSMDownloadLink("downloadSupportingDoc");
			downloadSupportingDoc.setVisible(false);

			if (Parameter.YES_NO_yes.equals(robTrainingTransaction.getIsHasLpoDoc())) {
				downloadSupportingDoc.setDownloadData(robTrainingTransaction.getTransactionCode() + "_LPO_DOCUMENT.pdf",
						"application/pdf", robTrainingTransaction.getLpoDocData().getFileData());
				downloadSupportingDoc.setVisible(true);
			}
			add(downloadSupportingDoc);

			if (Parameter.COMTRAC_TRANSACTION_STATUS_payment_success.equals(robTrainingTransaction.getStatus())) {
				printConfirmation.setVisible(true);
			} else {
				printConfirmation.setVisible(false);
			}

			SSMAjaxLink previous = new SSMAjaxLink("previous") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (fromPage instanceof SearchComtracList) {
						setResponsePage(SearchComtracList.class);
					} else if (fromPage instanceof NameListAttendees) {
						setResponsePage(new NameListAttendees(robTrainingTransaction.getTrainingId()));
					} else {
						setResponsePage(new TabComtracPage());
					}
				}
			};
			add(previous);

			wmcListParticipant.add(dataView);
			wmcListParticipant.add(new SSMPagingNavigator("navigator", dataView));
			wmcListParticipant.add(new NavigatorLabel("navigatorLabel", dataView));

		}

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

		/* System.out.println("AFTER WRITE OUTPUTSTREAM �---------"); */
		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, fileName);
		getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
	}
}
