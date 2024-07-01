package com.ssm.ezbiz.comtrac;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.model.LlpFileData;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.common.service.LlpFileDataService;
import com.ssm.llp.base.common.service.MailService;
import com.ssm.llp.base.odt.LLPOdtUtils;
import com.ssm.llp.base.page.SSMPagingNavigator;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSessionSortableDataProvider;
import com.ssm.llp.base.wicket.SSMDownloadLink;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMAjaxCheckBox;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMDropDownChoice;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobFormA;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.mod1.service.LlpUserProfileService;
import com.ssm.llp.page.supplyinfo.LlpSupplyInfoItemPanel;
import com.ssm.llp.wicket.SSMAjaxFormSubmitBehavior;

@SuppressWarnings({ "all" })
public class NameListAttendees extends SecBasePage {

	@SpringBean(name = "RobTrainingParticipantService")
	RobTrainingParticipantService robTrainingParticipantService;

	@SpringBean(name = "RobTrainingTransactionService")
	RobTrainingTransactionService robTrainingTransactionService;

	@SpringBean(name = "LlpUserProfileService")
	LlpUserProfileService llpUserProfileService;

	@SpringBean(name = "MailService")
	MailService mailService;

	@SpringBean(name = "LlpFileDataService")
	LlpFileDataService llpFileDataService;

	@Override
	public String getPageTitle() {
		return "page.lbl.ezbiz.nameListAttendees.tittle";
	}

	WebMarkupContainer listAttendees;
	List<RobTrainingParticipant> listParticipant;
	ModalWindow editAttendeesInfoPopUp;
	SSMAjaxCheckBox checkIsAttend;
	SSMAjaxCheckBox checkIsEligible;
	SSMAjaxCheckBox checkIsRefund;
	SSMDropDownChoice remarkAbsent;

	public NameListAttendees(final RobTrainingConfig robTrainingConfig) {

		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				SearchModel searchModel = new SearchModel();
				return searchModel;
			}
		}));
		add(new SearchParticipantForm("form", getDefaultModel(), robTrainingConfig));
	}

	public class SearchParticipantForm extends Form implements Serializable {
		public SearchParticipantForm(String id, IModel m, final RobTrainingConfig robTrainingConfig) {
			super(id, m);

			// final SearchModel searchModel
			final SearchModel searchModel = (SearchModel) m.getObject();

			editAttendeesInfoPopUp = new ModalWindow("editAttendeesInfoPopUp");
			editAttendeesInfoPopUp.setHeightUnit("px");
			editAttendeesInfoPopUp.setInitialHeight(500);
			add(editAttendeesInfoPopUp);
			populateData(null, robTrainingConfig, null);

			add(new SSMLabel("trainingCode", robTrainingConfig.getTrainingCode()));
			add(new SSMLabel("trainingName", robTrainingConfig.getTrainingName()));
			add(new SSMLabel("trainingDate", robTrainingConfig.getTrainingStartDt()));
			add(new SSMLabel("maxParticipant", robTrainingConfig.getMaxPax()));
			add(new SSMLabel("participantPayed",
					robTrainingTransactionService.countParticipantByStatusAndTrainingId(
							robTrainingConfig.getTrainingId(),
							new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_payment_success })));
			add(new SSMLabel("participantNotPay", robTrainingTransactionService.countParticipantByStatusAndTrainingId(
					robTrainingConfig.getTrainingId(), new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_data_entry,
							Parameter.COMTRAC_TRANSACTION_STATUS_pending_payment })));

			add(new SSMLabel("trainingType", robTrainingConfig.getTrainingType()));

			SSMAjaxLink previous = new SSMAjaxLink("previous") {
				@Override
				public void onClick(AjaxRequestTarget target) {

					setResponsePage(new ListComtracTraining());
				}
			};
			add(previous);

		}

		public void populateData(AjaxRequestTarget target, final RobTrainingConfig robTrainingConfig, String ic) {

			listAttendees = new WebMarkupContainer("listAttendees");
			listAttendees.setOutputMarkupId(true);
			listAttendees.setVisible(true);

			listParticipant = robTrainingParticipantService.findAllParticipantByTrainingIdStatus(
					robTrainingConfig.getTrainingId(),
					new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_payment_success }, ic);

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

					for (RobTrainingParticipant participant : listParticipant) {

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
			listAttendees.add(resendEmail);

			SSMAjaxLink emailBlast = new SSMAjaxLink("emailBlast") {
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

					String trainingStartDt = sdf.format(robTrainingConfig.getTrainingStartDt());

					for (RobTrainingParticipant participant : listParticipant) {

						if (!participant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")) {

							if (participant.getFileId() != null) {

								try {
									mailService.sendMail(participant.getEmail(),
											"email.notification.ecomtrac.eligible.subject",
											participant.getRobTrainingTransaction().getTransactionCode(),
											"email.notification.ecomtrac.eligible.body", participant.getName(),
											participant.getRobTrainingTransaction().getTrainingId().getTrainingName(),
											participant.getRobTrainingTransaction().getTrainingId().getTrainingCode(),
											trainingDt);
								} catch (Exception e) {
									e.printStackTrace();
								}

								System.out.println(participant.getRobTrainingTransaction().getTransactionCode() + " "
										+ "- Cert generated!");

							} else if (participant.getFileId() == null) {

								try {
									mailService.sendMail(participant.getEmail(),
											"email.notification.ecomtrac.noteligible.subject",
											participant.getRobTrainingTransaction().getTransactionCode(),
											"email.notification.ecomtrac.noteligible.body", participant.getName(),
											participant.getRobTrainingTransaction().getTrainingId().getTrainingName(),
											participant.getRobTrainingTransaction().getTrainingId().getTrainingCode(),
											trainingDt);
								} catch (Exception e) {
									e.printStackTrace();
								}

								System.out.println(participant.getRobTrainingTransaction().getTransactionCode() + " "
										+ "- No cert generated!");
							}
						}
					}
				}
			};
			emailBlast.setConfirmQuestion("page.comtrac.attendees.email.confirm");
//			emailBlast.setOutputMarkupId(true);
			listAttendees.add(emailBlast);

			SSMTextField icNo = new SSMTextField("ic");
			listAttendees.add(icNo);

			SSMAjaxButton search = new SSMAjaxButton("search") {
				@Override
				public void onSubmit(AjaxRequestTarget target, Form form) {
					SearchModel model = (SearchModel) form.getDefaultModelObject();
					populateData(target, robTrainingConfig, model.getIc());
				}
			};
			listAttendees.add(search);

			Link downloadPdf = new Link("downloadPdf") {
				@Override
				public void onClick() {

					Integer trainingIdNo = robTrainingConfig.getTrainingId();

					Map mapData = new HashMap();

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					String date = sdf.format(robTrainingConfig.getTrainingStartDt());

					mapData.put("loginName", UserEnvironmentHelper.getLoginName());
					mapData.put("trainingCode", robTrainingConfig.getTrainingCode());
					mapData.put("trainingName", robTrainingConfig.getTrainingName());
					mapData.put("trainingDate", date);
					mapData.put("maxPax", robTrainingConfig.getMaxPax());
					mapData.put("listParticipant", listParticipant);
					mapData.put("size", listParticipant.size());

					/*
					 * File file = new
					 * File("C:/Users/mhaziq/Desktop/SSM_List_of_Attendees_Report.odt");
					 */
					byte bytePdfContent[] = LLPOdtUtils.generatePdf("SSM_LIST_OF_ATTENDEES", mapData);
					generateDownload(robTrainingConfig.getTrainingCode() + " - Attendance.pdf", bytePdfContent);
				}
			};
			downloadPdf.setOutputMarkupPlaceholderTag(true);
			listAttendees.add(downloadPdf);

			SSMDownloadLink downloadExcel = new SSMDownloadLink("downloadExcel") {
				public void onClick() {
					if (getByteData() != null) {
						super.onClick();
						return;
					}
					try {
						String fileName = robTrainingConfig.getTrainingCode() + " - Attendance.xls";
						byte byteExcel[] = ((RobTrainingParticipantService) getService(
								RobTrainingParticipantService.class.getSimpleName()))
								.generateExcelParticipant(robTrainingConfig, listParticipant);
						if (byteExcel != null) {
							setDownloadData(fileName, SSMDownloadLink.TYPE_EXCEL, byteExcel);
							super.onClick();
						}
					} catch (Exception e) {
						ssmError(e.getMessage());
						e.printStackTrace();
					}
				}

			};
			listAttendees.add(downloadExcel);

			if (listParticipant.size() > 0) {
				emailBlast.setEnabled(true);
				resendEmail.setEnabled(true);
				downloadExcel.setEnabled(true);
				downloadPdf.setEnabled(true);

			} else {
				emailBlast.setEnabled(false);
				resendEmail.setEnabled(false);
				downloadExcel.setEnabled(false);
				downloadPdf.setEnabled(false);
			}

			SSMSessionSortableDataProvider dpAttendees = new SSMSessionSortableDataProvider("", listParticipant);
			SSMDataView<RobTrainingParticipant> dataViewAttendees = new SSMDataView<RobTrainingParticipant>(
					"sortingAttendees", dpAttendees) {

				@Override
				public void populateItem(final Item<RobTrainingParticipant> item) {
//					final WebMarkupContainer wmcAttend = new WebMarkupContainer("wmcAttend");
//					wmcAttend.setOutputMarkupPlaceholderTag(true);
//					item.add(wmcAttend);
//
//					final WebMarkupContainer wmcNotAttend = new WebMarkupContainer("wmcNotAttend");
//					wmcNotAttend.setOutputMarkupPlaceholderTag(true);
//					item.add(wmcNotAttend);

//					final WebMarkupContainer wmcEligible = new WebMarkupContainer("wmcEligible");
//					wmcEligible.setOutputMarkupPlaceholderTag(true);
//					item.add(wmcEligible);

					final RobTrainingParticipant listParticipant = item.getModelObject();

					item.add(new SSMLabel("transactionCode",
							listParticipant.getRobTrainingTransaction().getTransactionCode()));
					item.add(new SSMLabel("trainerName", listParticipant.getName()));
					item.add(new SSMLabel("trainerIc", listParticipant.getIcNo()));
					item.add(new SSMLabel("companyName", listParticipant.getCompany()));
					item.add(new SSMLabel("trainerNoTel", listParticipant.getTelNo()));
					item.add(new SSMLabel("trainerEmail", listParticipant.getEmail()));
					item.add(new SSMLabel("createBy", listParticipant.getRobTrainingTransaction().getLodgerName()));
					item.add(new SSMLabel("bil", item.getIndex() + 1));

					SSMAjaxLink viewAttendeesInfo = new SSMAjaxLink("viewAttendeesInfo", item.getDefaultModel()) {
						public void onClick(AjaxRequestTarget target) {

							editAttendeesInfoPopUp.setPageCreator(new ModalWindow.PageCreator() {
								@Override
								public Page createPage() {
									return new ViewAttendeesInfo(listParticipant, editAttendeesInfoPopUp);// edit record
								}
							});
							editAttendeesInfoPopUp.show(target);
						}
					};
					item.add(viewAttendeesInfo);

					SSMAjaxLink goToTransaction = new SSMAjaxLink("goToTransaction", item.getDefaultModel()) {
						public void onClick(AjaxRequestTarget target) {
							setResponsePage(new ViewListParticipantSummary(
									listParticipant.getRobTrainingTransaction().getTransactionCode(), getPage()));
						}
					};
					item.add(goToTransaction);

					SSMLabel unpaidLbl = new SSMLabel("unpaidLbl",
							resolve("page.lbl.ezbiz.nameListAttandees.unpaidLbl"));
					unpaidLbl.setVisible(false);
					item.add(unpaidLbl);

					checkIsAttend = new SSMAjaxCheckBox("checkIsAttend",
							new PropertyModel(listParticipant, "checkIsAttend")) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {

							if (String.valueOf(true).equals(getValue())) {

								listParticipant.setCheckIsAttend(true);
							} else {

								listParticipant.setCheckIsAttend(false);
							}
						}
					};
					item.add(checkIsAttend);

					checkIsEligible = new SSMAjaxCheckBox("checkIsEligible",
							new PropertyModel(listParticipant, "checkIsEligible")) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {

							if (String.valueOf(true).equals(getValue())) {

								listParticipant.setCheckIsEligible(true);
							} else {

								listParticipant.setCheckIsEligible(false);
							}
						}
					};
					item.add(checkIsEligible);

					SSMDownloadLink downloadCert = new SSMDownloadLink("downloadCert") {
						public void onClick() {
							RobTrainingParticipant participant = item.getModelObject();
							LlpFileData fileData = llpFileDataService.findById(participant.getFileId());

//							RobTrainingTransaction transaction = robTrainingTransactionService.findByTransactionCodeWithParticipant(robTrainingTransaction.getTransactionCode());
//							Map mapData = new HashMap();
//							mapData.put("robTrainingTransaction", transaction);
//							mapData.put("participantList", transaction.getRobTrainingParticipantList());
//							
//							byte bytePdfContent[] = LLPOdtUtils.generatePdf("COMTRAC_CONFIRMATION_SLIP", mapData);
							generateDownload(participant.getIcNo() + "_CERTIFICATE.pdf", fileData.getFileData());

						}
					};
					downloadCert.setOutputMarkupId(true);
					item.add(downloadCert);

					remarkAbsent = new SSMDropDownChoice("remarkAbsent",
							new PropertyModel(listParticipant, "remarkAbsent"), Parameter.REMARK_ABSENT);
					remarkAbsent.setOutputMarkupId(true);
					item.add(remarkAbsent);

					checkIsRefund = new SSMAjaxCheckBox("checkIsRefund",
							new PropertyModel(listParticipant, "checkIsRefund")) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {

							if (String.valueOf(true).equals(getValue())) {

								listParticipant.setCheckIsRefund(true);
								listParticipant.setIsRefund(Parameter.YES_NO_yes);
							} else {

								listParticipant.setCheckIsRefund(false);
								listParticipant.setIsRefund(Parameter.YES_NO_no);
							}
							;
							robTrainingParticipantService.update(listParticipant);
							setResponsePage(new NameListAttendees(robTrainingConfig));
						}
					};
					item.add(checkIsRefund);
					checkIsRefund.setEnabled(false);

					// status UNPAID tidak layak refund
					if (listParticipant.getRobTrainingTransaction().getPaymentChannel().equals("UNPAID")) {
						unpaidLbl.setVisible(true);
						checkIsAttend.setVisible(false);
						checkIsAttend.setEnabled(false);
						checkIsRefund.setEnabled(false);
						remarkAbsent.setVisible(false);
						listParticipant.setIsAttend(Parameter.YES_NO_yes);

						item.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>() {
							private static final long serialVersionUID = 1L;

							@Override
							public String getObject() {
								return "negative";
							}
						}));
					}

					if (listParticipant.getFileId() == null) {
						downloadCert.setVisible(false);
					} else {
						checkIsAttend.setEnabled(false);
						checkIsEligible.setVisible(false);
						checkIsRefund.setVisible(false);
						remarkAbsent.setVisible(false);
					}

					if (listParticipant.getIsAttend().equals("Y")) {
						listParticipant.setCheckIsAttend(true);
					} else {
						listParticipant.setCheckIsAttend(false);
						checkIsEligible.setEnabled(false);
						checkIsRefund.setEnabled(true);
					}

					if (listParticipant.getIsEligible() == null) {
						listParticipant.setCheckIsEligible(false);
					} else {
						if (listParticipant.getIsEligible().equals("Y")) {
							listParticipant.setCheckIsEligible(true);
						} else {
							listParticipant.setCheckIsEligible(false);
						}
					}

					if (listParticipant.getIsRefund() == null) {
						listParticipant.setCheckIsRefund(false);
					} else {
						if (listParticipant.getIsRefund().equals("Y")) {
							listParticipant.setCheckIsRefund(true);
							checkIsAttend.setEnabled(false);
							checkIsEligible.setEnabled(false);
						} else {
							listParticipant.setCheckIsRefund(false);
							remarkAbsent.setVisible(false);
						}
					}
				}
			};
			dataViewAttendees.setItemsPerPage(150L);

			listAttendees.add(dataViewAttendees);
			listAttendees.add(new SSMPagingNavigator("navigator", dataViewAttendees));
			listAttendees.add(new NavigatorLabel("navigatorLabel", dataViewAttendees));

			if (target == null) {
				add(listAttendees);
			} else {
				replace(listAttendees);
				target.add(listAttendees);
			}

			// update attend and eligible attendees
			SSMAjaxButton updateChanges = new SSMAjaxButton("updateChanges") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

					for (RobTrainingParticipant participant : listParticipant) {
//					for (int i = 0; i <100; i++) {
//						RobTrainingParticipant participant = listParticipant.get(i);

						if (!participant.getRobTrainingTransaction().getPaymentChannel().equals("UNPAID")) {

							if (participant.getCheckIsAttend().equals(true)) {

								participant.setIsAttend(Parameter.YES_NO_yes);
							} else {

								participant.setIsAttend(Parameter.YES_NO_no);
							}

							if (participant.getCheckIsEligible().equals(true) || participant.getFileId() != null) {

								participant.setIsEligible(Parameter.YES_NO_yes);
							} else {

								participant.setIsEligible(Parameter.YES_NO_no);
							}
						}

						if (participant.getRobTrainingTransaction().getPaymentChannel().equals("UNPAID")) {

							if (participant.getCheckIsEligible().equals(true) || participant.getFileId() != null) {

								participant.setIsEligible(Parameter.YES_NO_yes);
							} else {

								participant.setIsEligible(Parameter.YES_NO_no);
							}
						}
						robTrainingParticipantService.update(participant);
					}
					setResponsePage(new NameListAttendees(robTrainingConfig));
				}

			};
			listAttendees.add(updateChanges);

		}
	}

	public class SearchModel {
		public String ic;

		public String getIc() {
			return ic;
		}

		public void setIc(String ic) {
			this.ic = ic;
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
