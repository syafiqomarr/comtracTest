package com.ssm.ezbiz.comtrac;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.DateUtil;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ssm.ezbiz.comtrac.SelectComtracTraining.ComtracFormModel;
import com.ssm.ezbiz.robformA.ViewRobFormAOwnerPanel;
import com.ssm.ezbiz.service.RobTrainingFinalListingRemarkService;
import com.ssm.ezbiz.service.RobTrainingFinalListingService;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.model.LlpFileData;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.common.service.LlpFileDataService;
import com.ssm.llp.base.common.service.LlpPaymentTransactionService;
import com.ssm.llp.base.common.service.MailService;
import com.ssm.llp.base.odt.LLPOdtUtils;
import com.ssm.llp.base.page.SSMPagingNavigator;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSessionSortableDataProvider;
import com.ssm.llp.base.wicket.SSMDownloadLink;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextArea;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobCounterCollection;
import com.ssm.llp.ezbiz.model.RobFormNotes;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingFinalListing;
import com.ssm.llp.ezbiz.model.RobTrainingFinalListingRemark;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.mod1.model.RobUserOku;
import com.ssm.llp.mod1.service.LlpUserProfileService;

@SuppressWarnings({ "all" })
public class RegisterFinalListing extends SecBasePage {

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

	@SpringBean(name = "RobTrainingFinalListingService")
	RobTrainingFinalListingService robTrainingFinalListingService;

	@SpringBean(name = "RobTrainingFinalListingRemarkService")
	RobTrainingFinalListingRemarkService robTrainingFinalListingRemarkService;

	@Override
	public String getPageTitle() {
		return "page.lbl.ezbiz.nameListAttendees.tittle";
	}

	WebMarkupContainer listAttendees;
	List<RobTrainingParticipant> listParticipant;
	ModalWindow editAttendeesInfoPopUp;
	WebMarkupContainer summaryParticipant;

	public RobTrainingFinalListing robTrainingFinalListing;

	public RegisterFinalListing(final RobTrainingConfig robTrainingConfig) {

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

		}

		public void populateData(AjaxRequestTarget target, final RobTrainingConfig robTrainingConfig, String ic) {

			RobTrainingFinalListing existingFinalListing = robTrainingFinalListingService
					.findByTrainingCode(robTrainingConfig.getTrainingCode());

			listAttendees = new WebMarkupContainer("listAttendees");
			listAttendees.setOutputMarkupId(true);
			listAttendees.setVisible(true);
			
			summaryParticipant = new WebMarkupContainer("summaryParticipant");
			summaryParticipant.setOutputMarkupId(true);
			summaryParticipant.setVisible(true);

			listParticipant = robTrainingParticipantService.findAllParticipantByTrainingIdStatus(// buat new service
																									// kali
					robTrainingConfig.getTrainingId(),
					new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_payment_success }, ic);

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
					 * File("C:/Users/daneal/Desktop/SSM_List_of_Attendees_Report.odt");
					 */

					byte bytePdfContent[] = LLPOdtUtils.generatePdf("SSM_LIST_OF_ATTENDEES", mapData);
					generateDownload(robTrainingConfig.getTrainingCode() + " - Attendance.pdf", bytePdfContent);
				}
			};
			downloadPdf.setOutputMarkupPlaceholderTag(true);
			listAttendees.add(downloadPdf);
			downloadPdf.setVisible(false);

			SSMDownloadLink downloadExcel = new SSMDownloadLink("downloadExcel") {
//				public void onClick() {
//					if (getByteData() != null) {
//						super.onClick();
//						return;
//					}
//					try {
//						String fileName = robTrainingFinalListing.getTrainingCode() + " - Attendance.xls";
//						byte byteExcel[] = ((RobTrainingParticipantService) getService(
//								RobTrainingParticipantService.class.getSimpleName()))
//								.generateExcelParticipant(robTrainingFinalListing, listParticipant);
//						if (byteExcel != null) {
//							setDownloadData(fileName, SSMDownloadLink.TYPE_EXCEL, byteExcel);
//							super.onClick();
//						}
//					} catch (Exception e) {
//						ssmError(e.getMessage());
//						e.printStackTrace();
//					}
//				}

			};
			listAttendees.add(downloadExcel);
			downloadExcel.setVisible(false);

			SSMSessionSortableDataProvider dpAttendees = new SSMSessionSortableDataProvider("", listParticipant);

			SSMDataView<RobTrainingParticipant> dataViewAttendees = new SSMDataView<RobTrainingParticipant>(
					"sortingAttendees", dpAttendees) {

				@Override
				public void populateItem(final Item<RobTrainingParticipant> item) {

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
					item.add(new SSMLabel("remark", listParticipant.getRemarkAbsent()));
				}

			};
			dataViewAttendees.setItemsPerPage(150L);

			listAttendees.add(dataViewAttendees);
			listAttendees.add(new SSMPagingNavigator("navigator", dataViewAttendees));
			listAttendees.add(new NavigatorLabel("navigatorLabel", dataViewAttendees));

			if (target == null) {
				add(listAttendees);
				add(summaryParticipant);
			} else {
				replace(listAttendees);
				target.add(listAttendees);
				replace(summaryParticipant);
				target.add(summaryParticipant);
			}

			SSMAjaxLink back = new SSMAjaxLink("back") {
				@Override
				public void onClick(AjaxRequestTarget target) {

					setResponsePage(new ListComtracTraining());
				}
			};
			add(back);

			for (RobTrainingParticipant participant : listParticipant) {
				if (participant.getIsAttend() != null && participant.getIsAttend().equals("Y")) {
					countYesAttend++;
				}
				if (participant.getIsAttend() != null && participant.getIsAttend().equals("N")) {
					countNoAttend++;
				}
				if ((participant.getIsRefund() != null && participant.getIsRefund().equals("Y"))) {
					countYesRefund++;
				}
				if ((participant.getIsEligible()) != null && participant.getIsEligible().equals("Y")) {
					countYesEligible++;
				}
				if (participant.getRobTrainingTransaction().getRegistrationChannel()
						.equals(Parameter.COMTRAC_REGISTRATION_CHANNEL_ecomtrac)
						&& participant.getRobTrainingTransaction().getPaymentChannel()
								.equals(Parameter.COMTRAC_PAYMENT_CHANNEL_unpaid)) {
					countLpo++;
				}
			}
			summaryParticipant.add(new SSMLabel("countYesAttend", "Attend  : " + getCountYesAttend()));
			summaryParticipant.add(new SSMLabel("countNoAttend", "Absent  : " + getCountNoAttend()));
			summaryParticipant.add(new SSMLabel("countYesRefund", "Refund  : " + getCountYesRefund()));
			summaryParticipant.add(new SSMLabel("countLpo", "LPO     : " + getCountLpo()));
			summaryParticipant.add(new SSMLabel("countYesEligible", "Eligible for cert : " + getCountYesEligible()));

			// exec input remark
			SSMTextArea remark = new SSMTextArea("remark", Model.of(""));
			remark.setRequired(false);
			add(remark);

			SSMAjaxButton submit = new SSMAjaxButton("submit") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

					RobTrainingFinalListingRemark finalListingRemark = new RobTrainingFinalListingRemark();
					finalListingRemark.setFinalListingRefNo(existingFinalListing.getFinalListingRefNo());
					finalListingRemark.setQueryBy(UserEnvironmentHelper.getLoginName());
					if (remark.getDefaultModelObject() == null) {
						finalListingRemark.setRemark("");
					} else {
						finalListingRemark.setRemark(remark.getDefaultModelObject().toString());
					}
					robTrainingFinalListingRemarkService.insert(finalListingRemark);

					String alert = generateNotyAlert(
							resolve("page.lbl.ecomtrac.submitFinalListing", robTrainingConfig.getTrainingCode()),
							Parameter.ALERT_TYPE_info, target);
					setResponsePage(new FinalListingPage(alert, target));

				}
//					} catch (Exception e) {
//						// TODO: handle exception
//						e.printStackTrace();
//					}
//				}
			};
			submit.setOutputMarkupId(true);
			submit.setConfirmQuestion(resolve("page.lbl.ezbiz.comtrac.confirmToSubmit"));
			add(submit);
		}

	}

	private int countYesAttend;
	private int countNoAttend;
	private int countYesRefund;
	private int countYesEligible;
	private int countLpo;

	public int getCountYesAttend() {
		return countYesAttend;
	}

	public int getCountNoAttend() {
		return countNoAttend;
	}

	public int getCountYesRefund() {
		return countYesRefund;
	}

	public int getCountYesEligible() {
		return countYesEligible;
	}

	public int getCountLpo() {
		return countLpo;
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

		AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
			@Override
			public void write(OutputStream output) throws IOException {
				output.write(byteData);
				output.flush();
			}
		};

		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, fileName);
		getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
	}

}
