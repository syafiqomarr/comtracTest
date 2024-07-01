package com.ssm.ezbiz.comtrac;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.apache.wicket.util.resource.IResourceStream;

import com.ssm.ezbiz.comtrac.SelectLPOPayment.ComtracFormModel;
import com.ssm.ezbiz.incentive.ListIncentiveVerification;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.db.SearchCriteria;
import com.ssm.llp.base.common.db.SearchResult;
import com.ssm.llp.base.common.model.LlpFileData;
import com.ssm.llp.base.common.model.LlpPaymentTransaction;
import com.ssm.llp.base.common.model.LlpPaymentTransactionDetail;
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
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
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
public class ViewListLpoPage extends SecBasePage {

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

	public ViewListLpoPage(String transactionCode, final Page fromPage) {

		final RobTrainingTransaction robTrainingTransaction = robTrainingTransactionService
				.findByTransactionCodeWithParticipant(transactionCode);

		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				return robTrainingTransaction;
			}
		}));

		add(new ViewListLpoForm("form", getDefaultModel(), fromPage));
	}

	private class ViewListLpoForm extends Form implements Serializable {
		public ViewListLpoForm(String id, IModel m, final Page fromPage) {
			super(id, m);

			final RobTrainingTransaction robTrainingTransaction = (RobTrainingTransaction) m.getObject();

			final WebMarkupContainer wmcListParticipant = new WebMarkupContainer("wmcListParticipant");
			wmcListParticipant.setOutputMarkupId(true);
			wmcListParticipant.setOutputMarkupPlaceholderTag(true);
			add(wmcListParticipant);

//			Button save = null;
//			DecimalFormat df = new DecimalFormat("#,###,##0.00");

//			final WebMarkupContainer wmcListParticipant = new WebMarkupContainer("wmcListParticipant");
//			wmcListParticipant.setOutputMarkupId(true);
//			wmcListParticipant.setOutputMarkupPlaceholderTag(true);
//			add(wmcListParticipant);

//			final ModalWindow addParticipantPopup = new ModalWindow("addParticipantPopup");
//			addParticipantPopup.setWidthUnit("px");
//			addParticipantPopup.setHeightUnit("px");
//			addParticipantPopup.setInitialWidth(850);
//			addParticipantPopup.setInitialHeight(650);
//
//			add(addParticipantPopup);
//
//			addParticipantPopup.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
//				@Override
//				public void onClose(AjaxRequestTarget target) {
//					setResponsePage(new ViewListLpoPage(robTrainingTransaction.getTransactionCode(), fromPage));
//				}
//			});

//			SSMTextField louLoaRefNo = new SSMTextField("louLoaRefNo");
//			louLoaRefNo.setOutputMarkupId(true);
//			louLoaRefNo.setVisible(false);
//			add(louLoaRefNo);

//			SSMTextField invoiceNo = new SSMTextField("invoiceNo");
//			invoiceNo.setOutputMarkupId(true);
//			invoiceNo.setVisible(false);
//			add(invoiceNo);

//			SSMTextField receiptNo = new SSMTextField("receiptNo");
//			receiptNo.setOutputMarkupId(true);
//			receiptNo.setVisible(false);
//			add(receiptNo);

//			SSMTextArea remarks = new SSMTextArea("remarks");
//			remarks.setOutputMarkupId(true);
//			remarks.setVisible(true);
//			add(remarks);

			add(new SSMLabel("transactionCode", robTrainingTransaction.getTransactionCode()));
			add(new SSMLabel("trainingCode", robTrainingTransaction.getTrainingId().getTrainingCode()));
//			add(new SSMLabel("trainingName", robTrainingTransaction.getTrainingId().getTrainingName()));
//			add(new SSMLabel("lodgerName", robTrainingTransaction.getLodgerName()));
//			add(new SSMLabel("icNo", robTrainingTransaction.getLodgerId()));
//			add(new SSMLabel("createDt", robTrainingTransaction.getCreateDt(), "dd/MM/yyyy hh:mm a"));
//			add(new SSMLabel("trainingStartTime", robTrainingConfig.getTrainingStartTime()));
//			add(new SSMLabel("trainingEndTime", robTrainingConfig.getTrainingEndTime()));
//			add(new SSMLabel("cpePoint", robTrainingConfig.getCpePoint()));
//			add(new SSMLabel("trainingVenue", robTrainingConfig.getTrainingVenue()));
			add(new SSMLabel("totalPax", robTrainingTransaction.getTotalPax()));
			add(new SSMLabel("amountWithoutGst",
					robTrainingTransaction.getAmount() - robTrainingTransaction.getGstAmount()));
			add(new SSMLabel("gstAmount", robTrainingTransaction.getGstAmount()));
			add(new SSMLabel("amountIncludeGst", robTrainingTransaction.getAmount()));
			add(new SSMLabel("totalAll", robTrainingTransaction.getAmount() + robTrainingTransaction.getGstAmount()));

//			String receipt = "-";
//			String invoice = "-";
//			String lpoloa = "-";

//			if (!StringUtils.isBlank(robTrainingTransaction.getLouLoaRefNo())) {
//				lpoloa = robTrainingTransaction.getLouLoaRefNo();
//			}
//
//			if (!StringUtils.isBlank(robTrainingTransaction.getInvoiceNo())) {
//				invoice = robTrainingTransaction.getInvoiceNo();
//			}
//
//			if (!StringUtils.isBlank(robTrainingTransaction.getReceiptNo())) {
//				receipt = robTrainingTransaction.getReceiptNo();
//			}

//			SSMLabel louLoaRefNoLabel = new SSMLabel("louLoaRefNoLabel", lpoloa);
//			louLoaRefNoLabel.setOutputMarkupId(true);
//			louLoaRefNoLabel.setVisible(false);
//			add(louLoaRefNoLabel);
//
//			SSMLabel invoiceLabel = new SSMLabel("invoiceLabel", invoice);
//			invoiceLabel.setOutputMarkupId(true);
//			invoiceLabel.setVisible(false);
//			add(invoiceLabel);
//
//			SSMLabel receiptLabel = new SSMLabel("receiptLabel", receipt);
//			receiptLabel.setOutputMarkupId(true);
//			receiptLabel.setVisible(false);
//			add(receiptLabel);

//				SSMDropDownChoice status = new SSMDropDownChoice("status", Parameter.COMTRAC_TRANSACTION_STATUS);
//				status.setOutputMarkupId(true);
//				status.setVisible(true);
//				add(status);

//				SSMDropDownChoice paymentChannel = new SSMDropDownChoice("paymentChannel", Parameter.COMTRAC_PAYMENT_CHANNEL);
//				add(paymentChannel);

//			String statusDesc = getCodeTypeWithValue(Parameter.COMTRAC_TRANSACTION_STATUS,
//					robTrainingTransaction.getStatus());
//			String paymentChannelDesc = getCodeTypeWithValue(Parameter.COMTRAC_PAYMENT_CHANNEL,
//					robTrainingTransaction.getPaymentChannel());

//			SSMTextField status = new SSMTextField("status", new PropertyModel(statusDesc, ""));
//			status.setOutputMarkupId(true);
//			status.setVisible(true);
//			status.setReadOnly(true);
//			add(status);

//			SSMTextField paymentChannel = new SSMTextField("paymentChannel", new PropertyModel(paymentChannelDesc, ""));
//			paymentChannel.setReadOnly(true);
//			add(paymentChannel);

//			SSMLabel remarkLabel = new SSMLabel("remarkLabel", robTrainingTransaction.getRemarks());
//			remarkLabel.setOutputMarkupId(true);
//			remarkLabel.setVisible(false);
//			add(remarkLabel);

//			save = new Button("save") {
//				public void onSubmit() {
//					RobTrainingTransaction model = (RobTrainingTransaction) getForm().getModelObject();
//
//					if (Parameter.COMTRAC_TRANSACTION_STATUS_cancel.equals(model.getStatus())) {
//						RobTrainingConfig tempObject = robTrainingConfigService
//								.findById(model.getTrainingId().getTrainingId());
//						Integer currentpax = tempObject.getCurrentPax();
//						tempObject.setCurrentPax(currentpax - 1);
//						robTrainingConfigService.update(tempObject);
//					}
//					robTrainingTransactionService.update(model);
//
//					setResponsePage(new ViewListLpoPage(model.getTransactionCode(), fromPage));
//				}
//			};
//			add(save);

//			if (UserEnvironmentHelper.isInternalUser()) {
//
//				save.setVisible(true);
//				louLoaRefNo.setVisible(true);
//				/* louLoaRefNoLabel.setVisible(false); */
//				receiptNo.setVisible(true);
//				/* receiptLabel.setVisible(false); */
//				invoiceNo.setVisible(true);
//				/* invoiceLabel.setVisible(false); */
//				status.setVisible(true);
//
//			} else {
//
//				save.setVisible(false);
//				louLoaRefNo.setVisible(false);
//				louLoaRefNoLabel.setVisible(true);/
//				receiptNo.setVisible(false);
//				receiptLabel.setVisible(true);
//				invoiceNo.setVisible(false);
//				invoiceLabel.setVisible(true);
//				status.setVisible(false);
//			}

//			if (Parameter.COMTRAC_TRANSACTION_STATUS_cancel.equals(robTrainingTransaction.getStatus())) {
//				save.setVisible(false);
//				louLoaRefNo.setVisible(false);
//				louLoaRefNoLabel.setVisible(true);
//				receiptNo.setVisible(false);
//				receiptLabel.setVisible(true);
//				invoiceNo.setVisible(false);
//				invoiceLabel.setVisible(true);
//				if (UserEnvironmentHelper.isInternalUser()) {
//					remarkLabel.setVisible(true);
//				} else {
//					remarkLabel.setVisible(false);
//				}
//
//				status.setVisible(false);
//			}

//			if (!robTrainingTransaction.getLodgerId().equals("SSM STAF")) {
//				status.setVisible(false);
//			}
//			add(new SSMLabel("totalPax", robTrainingTransaction.getTotalPax()));
//			add(new SSMLabel("amount", df.format(robTrainingTransaction.getAmount())));

//			SSMAjaxLink resendEmail = new SSMAjaxLink("resendEmail") {
//				@Override
//				public void onClick(AjaxRequestTarget target) {
//
//					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//					DecimalFormat df = new DecimalFormat("#,###,##0.00");
//
//					String lodgerName = "us";
//					String lodgerPhone = "603-7721 4000";
//
//					String trainingDt = sdf.format(robTrainingConfig.getTrainingStartDt());
//					if (!robTrainingConfig.getTrainingEndDt().equals(robTrainingConfig.getTrainingStartDt())) {
//						trainingDt += " - " + sdf.format(robTrainingConfig.getTrainingEndDt());
//					}
//
//					for (RobTrainingParticipant participant : robTrainingTransaction.getRobTrainingParticipantList()) {
//
//						if (!participant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")) {
//							LlpUserProfile llpUserProfile = llpUserProfileService
//									.findProfileInfoByUserId(participant.getRobTrainingTransaction().getLodgerName());
//							lodgerName = llpUserProfile.getName();
//							lodgerPhone = llpUserProfile.getHpNo();
//						}
//
//						mailService.sendMail(participant.getEmail(), "email.notification.comtrac.confirmation.subject",
//								participant.getRobTrainingTransaction().getTransactionCode(),
//								"email.notification.comtrac.confirmation.body",
//								participant.getRobTrainingTransaction().getTrainingId().getTrainingName(),
//								participant.getRobTrainingTransaction().getTrainingId().getTrainingCode(), trainingDt,
//								participant.getRobTrainingTransaction().getTrainingId().getTrainingDesc(), lodgerName,
//								lodgerPhone);
//					}
//				}
//			};
//			resendEmail.setConfirmQuestion("page.comtrac.attendees.email.confirm");
//			wmcListParticipant.add(resendEmail);

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

//					AjaxLink viewInfo = new AjaxLink("viewInfo", item.getDefaultModel()) {
//						public void onClick(AjaxRequestTarget target) {
//
//							addParticipantPopup.setPageCreator(new ModalWindow.PageCreator() {
//								@Override
//								public Page createPage() {
//									if (UserEnvironmentHelper.isInternalUser()
//											&& Parameter.COMTRAC_TRANSACTION_STATUS_payment_success
//													.equals(robTrainingTransaction.getStatus())) {
//										return new AddTrainingParticipantForm(robTrainingParticipant, robTrainingConfig,
//												addParticipantPopup, item.getIndex(), true, getPage());// edit record
//									} else {
//										return new ViewAttendeesInfo(robTrainingParticipant, addParticipantPopup);// edit
//																													// record
//									}
//								}
//							});
//							addParticipantPopup.show(target);
//						}
//					};
//					item.add(viewInfo);

//					SSMAjaxLink delete = new SSMAjaxLink("delete") {
//						public void onClick(AjaxRequestTarget target) {
//							RobTrainingParticipant robTrainingParticipant = item.getModelObject();
//
//							Double tempAmount = robTrainingParticipant.getAmount();
//							Double tempGstAmount = robTrainingParticipant.getGstAmount();
//
//							// delete participant
//							robTrainingParticipantService
//									.deleteUsingParticipantId(robTrainingParticipant.getParticipantId());
//
//							// update data in transaction
//							robTrainingTransaction.setAmount(robTrainingTransaction.getAmount() - tempAmount);
//							robTrainingTransaction.setGstAmount(robTrainingTransaction.getGstAmount() - tempGstAmount);
//							robTrainingTransaction.setTotalPax(robTrainingTransaction.getTotalPax() - 1);
//							robTrainingTransactionService.update(robTrainingTransaction);
//
//							// update data in training config
//							RobTrainingConfig config = robTrainingConfigService
//									.findById(robTrainingTransaction.getTrainingId().getTrainingId());
//							config.setCurrentPax(config.getCurrentPax() - 1);
//							robTrainingConfigService.update(config);
//
//							setResponsePage(new ViewListLpoPage(robTrainingTransaction.getTransactionCode(), fromPage));
//						}
//					};
//					delete.setConfirmQuestion("participant.confirmDelete");
//					delete.setOutputMarkupId(true);
//					item.add(delete);

//					SSMDownloadLink downloadCert = new SSMDownloadLink("downloadCert") {
//						public void onClick() {
//							RobTrainingParticipant robTrainingParticipant = item.getModelObject();
//							LlpFileData fileData = llpFileDataService.findById(robTrainingParticipant.getFileId());
//
////							RobTrainingTransaction transaction = robTrainingTransactionService.findByTransactionCodeWithParticipant(robTrainingTransaction.getTransactionCode());
////							Map mapData = new HashMap();
////							mapData.put("robTrainingTransaction", transaction);
////							mapData.put("participantList", transaction.getRobTrainingParticipantList());
////							
////							byte bytePdfContent[] = LLPOdtUtils.generatePdf("COMTRAC_CONFIRMATION_SLIP", mapData);
//							generateDownload(robTrainingParticipant.getIcNo() + "_CERTIFICATE.pdf",
//									fileData.getFileData());
//
//						}
//					};
//					downloadCert.setOutputMarkupId(true);
//					item.add(downloadCert);

//					if (robTrainingParticipant.getFileId() == null) {
//						downloadCert.setVisible(false);
//					}

//					if (UserEnvironmentHelper.isInternalUser()
//							&& Parameter.COMTRAC_TRANSACTION_STATUS_payment_success
//									.equals(robTrainingTransaction.getStatus())
//							&& robTrainingParticipant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")) {
//						delete.setVisible(true);
//					} else {
//						delete.setVisible(false);
//					}

				}

			};

			SSMAjaxLink approve = new SSMAjaxLink("approve") {
				@Override
				public void onClick(AjaxRequestTarget target) {

					robTrainingTransaction.setStatus(Parameter.COMTRAC_TRANSACTION_STATUS_payment_success);
					robTrainingTransactionService.updateInsertAll(robTrainingTransaction);

					String alert = generateNotyAlert(
							resolve("page.lbl.ezbiz.lpo.approved", robTrainingTransaction.getTransactionCode()),
							Parameter.ALERT_TYPE_success, target);

					setResponsePage(new LpoApprovalTray());
//					setResponsePage(new LpoApprovalTray(alert, target));

					robTrainingTransactionService.sendEmailLpoApproved(robTrainingTransaction.getTransactionCode());// send
																													// email
																													// noti
																													// approved

				}
			};
			approve.setOutputMarkupId(true);
			approve.setOutputMarkupPlaceholderTag(true);
			add(approve);

			SSMAjaxLink cancel = new SSMAjaxLink("cancel") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setResponsePage(new LpoApprovalTray());
				}
			};
			cancel.setOutputMarkupId(true);
			add(cancel);

			SSMAjaxLink reject = new SSMAjaxLink("reject") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					robTrainingTransaction.setStatus(Parameter.COMTRAC_TRANSACTION_STATUS_reject);
					robTrainingTransactionService.updateInsertAll(robTrainingTransaction);

					String alert = generateNotyAlert(
							resolve("page.lbl.ezbiz.lpo.cancelled", robTrainingTransaction.getTransactionCode()),
							Parameter.ALERT_TYPE_alert, target);

					setResponsePage(new LpoApprovalTray());
//					setResponsePage(new LpoApprovalTray(alert, target));
//					wmcRemarks.setVisible(true);
//					remarksType.setDefaultModelObject("Reject");
//					target.add(wmcRemarks);

					robTrainingTransactionService.sendEmailLpoRejected(robTrainingTransaction.getTransactionCode());// send
																													// email
																													// noti
																													// rejected
					// delete participant in transaction code
					SearchResult sr = robTrainingParticipantService
							.findByCriteria(new SearchCriteria("robTrainingTransaction.transactionCode",
									SearchCriteria.EQUAL, robTrainingTransaction.getTransactionCode()));
					if (sr.getList().size() > 0) {
						List<RobTrainingParticipant> listToDelete = sr.getList();

						for (int i = 0; i < listToDelete.size(); i++) {
							robTrainingParticipantService
									.deleteUsingParticipantId(listToDelete.get(i).getParticipantId());
						}
						
						RobTrainingConfig tempObject = robTrainingConfigService
								.findById(robTrainingTransaction.getTrainingId().getTrainingId());
						Integer currentpax = tempObject.getCurrentPax();
						tempObject.setCurrentPax(currentpax - 1);
						robTrainingConfigService.update(tempObject);
					}
				}
			};
			reject.setOutputMarkupId(true);
			reject.setOutputMarkupPlaceholderTag(true);
			reject.setVisible(true);
			add(reject);

			// Download Supp Doc
			SSMDownloadLink downloadSupportingDoc = new SSMDownloadLink("downloadSupportingDoc");
			downloadSupportingDoc.setVisible(false);

			if (Parameter.YES_NO_yes.equals(robTrainingTransaction.getIsHasLpoDoc())) {

				downloadSupportingDoc.setDownloadData(robTrainingTransaction.getTransactionCode() + "_LPO_DOCUMENT.pdf",
						"application/pdf", robTrainingTransaction.getLpoDocData().getFileData());

//				RobTrainingTransaction robTrainingTransactions = (RobTrainingTransaction) getModelObject();
//
//				// new for lpo id using LAZY
//				LlpFileData fileData = llpFileDataService.findById(robTrainingTransactions.getLpoId());
//				generateDownload(robTrainingTransactions.getTransactionCode() + "_LPO_DOCUMENT.pdf",
//						fileData.getFileData());

				downloadSupportingDoc.setVisible(true);
			}
			add(downloadSupportingDoc);

//			SSMAjaxLink previous = new SSMAjaxLink("previous") {
//				@Override
//				public void onClick(AjaxRequestTarget target) {
//					if (fromPage instanceof SearchComtracList) {
//						setResponsePage(SearchComtracList.class);
//					} else if (fromPage instanceof NameListAttendees) {
//						setResponsePage(new NameListAttendees(robTrainingTransaction.getTrainingId()));
//					} else {
//						setResponsePage(new TabComtracPage());
//					}
//				}
//			};
//			add(previous);
//
			wmcListParticipant.add(dataView);
			wmcListParticipant.add(new SSMPagingNavigator("navigator", dataView));
			wmcListParticipant.add(new NavigatorLabel("navigatorLabel", dataView));

			if (robTrainingTransaction.getStatus().equals(Parameter.COMTRAC_TRANSACTION_STATUS_payment_success)
					|| robTrainingTransaction.getStatus().equals(Parameter.COMTRAC_TRANSACTION_STATUS_cancel)
					|| robTrainingTransaction.getStatus().equals(Parameter.COMTRAC_TRANSACTION_STATUS_reject)) {
				approve.setVisible(false);
				reject.setVisible(false);
			}

		}

	}

	private List<RobTrainingParticipant> reUpdateListParticipant(RobTrainingTransaction robTrainingTransaction) {
		List<RobTrainingParticipant> listParticipant = (List<RobTrainingParticipant>) getSession()
				.getAttribute("listParticipant_");
		if (listParticipant == null) {
			getSession().setAttribute("listParticipant_",
					(Serializable) robTrainingTransaction.getRobTrainingParticipantList());
			listParticipant.addAll(robTrainingTransaction.getRobTrainingParticipantList());
		}

		if (robTrainingTransaction.getRobTrainingParticipantList() != null) {
			for (RobTrainingParticipant participantDb : robTrainingTransaction.getRobTrainingParticipantList()) {
				if (!listParticipant.contains(participantDb)) {
					listParticipant.add(participantDb);
				}
			}
		}

		return listParticipant;

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

//	public void generateDownload(String fileName, final byte[] byteData){
//		IResourceStream resourceStream = new AbstractResourceStreamWriter() {
//		      @Override 
//		      public void write(OutputStream output) {
//		    	  try {
//					output.write(byteData);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//		      }
//
//		      @Override
//		      public String getContentType() {                        
//		        return "application/pdf";
//		      }
//		};
//		getRequestCycle().scheduleRequestHandlerAfterCurrent(new ResourceStreamRequestHandler(resourceStream).setFileName(fileName));
//		
////		AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
////			@Override
////			public void write(OutputStream output) throws IOException {
////				output.write(byteData);
////			}
////		};
////
////		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, fileName);
////		handler.setContentDisposition(ContentDisposition.ATTACHMENT);
////		getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
//	}

//	public void generateDownload(String fileName, final byte[] byteData) {
//
//		/* System.out.println("file Name :::::::::: " + fileName); */
//		// System.out.println("byte length "+new String(byteData, 0));
//
//		AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
//			@Override
//			public void write(OutputStream output) throws IOException {
//				output.write(byteData);
//				output.flush();
//			}
//		};
//
//		/* System.out.println("AFTER WRITE OUTPUTSTREAM �---------"); */
//		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, fileName);
//		getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
//	}

	@Override
	public String getPageTitle() {
		return "page.lbl.comtrac.lpo.feeApproval";
	}
}
