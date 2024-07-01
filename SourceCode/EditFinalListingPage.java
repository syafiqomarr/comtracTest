package com.ssm.ezbiz.comtrac;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.text.html.StyleSheet.ListPainter;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ssm.ezbiz.comtrac.SelectComtracTraining.ComtracFormModel;
import com.ssm.ezbiz.robformA.ViewRobFormAOwnerPanel;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingFinalListingRemarkService;
import com.ssm.ezbiz.service.RobTrainingFinalListingService;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.db.SearchCriteria;
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
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingFinalListing;
import com.ssm.llp.ezbiz.model.RobTrainingFinalListingRemark;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.mod1.service.LlpUserProfileService;

@SuppressWarnings({ "all" })
public class EditFinalListingPage extends SecBasePage {

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

	@SpringBean(name = "RobTrainingConfigService")
	RobTrainingConfigService robTrainingConfigService;

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

	public EditFinalListingPage(final RobTrainingFinalListing robTrainingFinalListing) {

		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				SearchModel searchModel = new SearchModel();
				return searchModel;
			}
		}));
		add(new SearchParticipantForm("form", getDefaultModel(), robTrainingFinalListing));
	}

	public class SearchParticipantForm extends Form implements Serializable {
		public SearchParticipantForm(String id, IModel m, final RobTrainingFinalListing robTrainingFinalListing) {
			super(id, m);

			final SearchModel searchModel = (SearchModel) m.getObject();

			RobTrainingConfig robTrainingConfig = robTrainingConfigService
					.findByTrainingCode(robTrainingFinalListing.getTrainingCode());

			editAttendeesInfoPopUp = new ModalWindow("editAttendeesInfoPopUp");
			editAttendeesInfoPopUp.setHeightUnit("px");
			editAttendeesInfoPopUp.setInitialHeight(500);
			add(editAttendeesInfoPopUp);

			populateData(null, robTrainingFinalListing, null);

			add(new SSMLabel("finalListingRefNo", robTrainingFinalListing.getFinalListingRefNo()));
			add(new SSMLabel("trainingCode", robTrainingFinalListing.getTrainingCode()));
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

		public void populateData(AjaxRequestTarget target, final RobTrainingFinalListing robTrainingFinalListing,
				String ic) {

			RobTrainingFinalListingRemark finalListingRemark = robTrainingFinalListingRemarkService
					.findExistingFinalListingRefNo(robTrainingFinalListing.getFinalListingRefNo());

			String userFilterFinance = getCodeTypeWithValue(Parameter.LLP_CONFIG,
					Parameter.LLP_CONFIG_FILTER_USER_LIST_FINANCE);
			String userFilterHosComtrac = getCodeTypeWithValue(Parameter.LLP_CONFIG,
					Parameter.LLP_CONFIG_FILTER_USER_LIST_HOS_COMTRAC);

			List<String> userListFinance = Arrays.asList(StringUtils.split(userFilterFinance, ","));
			List<String> userListHosComtrac = Arrays.asList(StringUtils.split(userFilterHosComtrac, ","));

			RobTrainingConfig robTrainingConfig = robTrainingConfigService
					.findByTrainingCode(robTrainingFinalListing.getTrainingCode());

			listAttendees = new WebMarkupContainer("listAttendees");
			listAttendees.setOutputMarkupId(true);
			listAttendees.setVisible(true);
			
			summaryParticipant = new WebMarkupContainer("summaryParticipant");
			summaryParticipant.setOutputMarkupId(true);
			summaryParticipant.setVisible(true);

			listParticipant = robTrainingParticipantService.findAllParticipantByTrainingIdStatus(
					robTrainingConfig.getTrainingId(),
					new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_payment_success }, ic);

			SSMTextField icNo = new SSMTextField("ic");
			listAttendees.add(icNo);
			
			// exec input remark
			SSMTextArea remark = new SSMTextArea("remark", Model.of(""));
			remark.setRequired(false);
			add(remark);

			SSMAjaxButton search = new SSMAjaxButton("search") {
				@Override
				public void onSubmit(AjaxRequestTarget target, Form form) {
					SearchModel model = (SearchModel) form.getDefaultModelObject();
					populateData(target, robTrainingFinalListing, model.getIc());
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
					mapData.put("trainingCode", robTrainingFinalListing.getTrainingCode());
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
					generateDownload(robTrainingFinalListing.getTrainingCode() + " - Attendance.pdf", bytePdfContent);
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
						String fileName = robTrainingConfig.getTrainingCode() + " - Final Listing.xls";
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

			SSMSessionSortableDataProvider dpAttendees = new SSMSessionSortableDataProvider("", listParticipant);

			SSMDataView<RobTrainingParticipant> dataViewAttendees = new SSMDataView<RobTrainingParticipant>(
					"sortingAttendees", dpAttendees) {

				@Override
				public void populateItem(final Item<RobTrainingParticipant> item) {

					final RobTrainingParticipant listParticipant = item.getModelObject();

					Map<String, Integer> participantCounts = new HashMap<>();

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

					setResponsePage(new FinalListingPage());
				}
			};
			add(back);

//			SSMTextArea latestRemark = new SSMTextArea("latestRemark", Model.of(""));
//			latestRemark.setDefaultModelObject(robTrainingFinalListing.getLatestRemark());
//			latestRemark.setEnabled(false);
//			add(latestRemark);

			for (RobTrainingParticipant participant : listParticipant) {
				if (participant.getIsAttend().equals("Y")) {
					countYesAttend++;
				}
				if (participant.getIsAttend().equals("N")) {
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
			

			List listFinalListingRemark = robTrainingFinalListingRemarkService
					.findByFinalListingRefNo(robTrainingFinalListing.getFinalListingRefNo());
			ListView<RobTrainingFinalListingRemark> listQueryView = new ListView("listQueryView",
					listFinalListingRemark) {
				@Override
				protected void populateItem(ListItem item) {
					RobTrainingFinalListingRemark robTrainingFinalListingRemark = (RobTrainingFinalListingRemark) item
							.getModelObject();
					item.add(new Label("queryBy", robTrainingFinalListingRemark.getQueryBy()));
					item.add(new MultiLineLabel("remark", robTrainingFinalListingRemark.getRemark()));
					item.add(new SSMLabel("createDt", robTrainingFinalListingRemark.getCreateDt(),
							"dd/MM/yyyy hh:mm:ss a"));

					int itemCount = item.size();
				}

			};
			add(listQueryView);
	
				SSMAjaxButton resubmit = new SSMAjaxButton("resubmit") {
					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
	
						if (remark != null && remark.getDefaultModelObject() != null) {
						    finalListingRemark.setRemark(remark.getDefaultModelObject().toString());
						    finalListingRemark.setQueryBy(UserEnvironmentHelper.getLoginName());
						} else {
						    finalListingRemark.setRemark("");
						}
						robTrainingFinalListing.setStatus(Parameter.COMTRAC_FINAL_LISTING_STATUS_pending);
	
						robTrainingFinalListingService.updateInsertAll(robTrainingFinalListing);
						robTrainingFinalListingRemarkService.insert(finalListingRemark);
	
						setResponsePage(new FinalListingPage());
					}
				};
				resubmit.setConfirmQuestion(resolve("page.lbl.ezbiz.comtrac.confirmToSubmit"));
				add(resubmit);
	
				SSMAjaxButton query = new SSMAjaxButton("query") {
					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
	
	//					RobTrainingFinalListingRemark finalListingRemark = new RobTrainingFinalListingRemark();
	//					robTrainingFinalListing.setLatestRemark(remark.getDefaultModelObject().toString());
	//					finalListingRemark.setFinalListingRefNo(robTrainingFinalListing.getFinalListingRefNo());
	//					finalListingRemark.setQueryBy(UserEnvironmentHelper.getLoginName());
	
						if (remark != null && remark.getDefaultModelObject() != null) {
						    finalListingRemark.setRemark(remark.getDefaultModelObject().toString());
						} else {
						    finalListingRemark.setRemark("");
						}
						robTrainingFinalListing.setStatus(Parameter.COMTRAC_FINAL_LISTING_STATUS_query);
	
						robTrainingFinalListingService.updateInsertAll(robTrainingFinalListing);
						robTrainingFinalListingRemarkService.insert(finalListingRemark);
	
						setResponsePage(new FinalListingPage());
					}
				};
				query.setConfirmQuestion(resolve("page.lbl.ezbiz.comtrac.confirmToSubmit"));
				add(query);

			SSMAjaxButton approve = new SSMAjaxButton("approve") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

//					robTrainingFinalListing.setStatus(Parameter.COMTRAC_FINAL_LISTING_STATUS_approved);
//					robTrainingFinalListingService.updateInsertAll(robTrainingFinalListing);

					for (RobTrainingParticipant participant : listParticipant) {
						if (participant.getIsRefund() != null && participant.getIsRefund().equals("Y")) {
							robTrainingFinalListingService.sendEmailRefundComtrac(
									participant.getRobTrainingTransaction().getTransactionCode());

//							System.out.println("REFUND: " + participant.getRobTrainingTransaction().getTransactionCode()
//									+ participant.getIsRefund());
						}
					}
					String alert = generateNotyAlert(
							resolve("page.lbl.comtrac.finalListing.approved",
									robTrainingFinalListing.getFinalListingRefNo()),
							Parameter.ALERT_TYPE_success, target);

					if (remark != null && remark.getDefaultModelObject() != null) {
					    finalListingRemark.setRemark(remark.getDefaultModelObject().toString());
					} else {
					    finalListingRemark.setRemark("");
					}
					robTrainingFinalListing.setStatus(Parameter.COMTRAC_FINAL_LISTING_STATUS_approved);

					robTrainingFinalListingService.updateInsertAll(robTrainingFinalListing);
					robTrainingFinalListingRemarkService.insert(finalListingRemark);

					setResponsePage(new FinalListingPage(alert, target));
				}
			};
			approve.setConfirmQuestion(resolve("page.lbl.ezbiz.comtrac.confirmToSubmit"));
			add(approve);

			SSMAjaxButton acknowledge = new SSMAjaxButton("acknowledge") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

					robTrainingFinalListing.setStatus(Parameter.COMTRAC_FINAL_LISTING_STATUS_acknowledge);

					robTrainingFinalListingService.updateInsertAll(robTrainingFinalListing);
					setResponsePage(new FinalListingPage());
				}
			};
			acknowledge.setConfirmQuestion(resolve("page.lbl.ezbiz.comtrac.confirmToSubmit"));
			add(acknowledge);

			if (StringUtils.isNotBlank(userFilterFinance) && StringUtils.isNotBlank(userFilterHosComtrac)) {

				if (!userListFinance.contains(UserEnvironmentHelper.getLoginName())
						&& !userListHosComtrac.contains(UserEnvironmentHelper.getLoginName())) {
					if (robTrainingFinalListing.getStatus().equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_approved)) {

						resubmit.setVisible(false);
						query.setVisible(false);
						approve.setVisible(false);
						remark.setVisible(false);
						acknowledge.setVisible(false);
					} else if (robTrainingFinalListing.getStatus()
							.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_pending)) {

						resubmit.setVisible(false);
						acknowledge.setVisible(false);
						downloadPdf.setVisible(false);
						downloadExcel.setVisible(false);
						remark.setVisible(false);
						query.setVisible(false);
						approve.setVisible(false);
					} else if (robTrainingFinalListing.getStatus()
							.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_query)) {

						query.setVisible(false);
						approve.setVisible(false);
						acknowledge.setVisible(false);
						downloadPdf.setVisible(false);
						downloadExcel.setVisible(false);
					} else if (robTrainingFinalListing.getStatus()
							.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_acknowledge)) {

						resubmit.setVisible(false);
						query.setVisible(false);
						approve.setVisible(false);
						acknowledge.setVisible(false);
						remark.setVisible(false);
					}

				} else if (userListFinance.contains(UserEnvironmentHelper.getLoginName())) {
					if (robTrainingFinalListing.getStatus().equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_approved)) {

						resubmit.setVisible(false);
						query.setVisible(false);
						approve.setVisible(false);
						remark.setVisible(false);
						listQueryView.setVisible(false);
						summaryParticipant.setVisible(false);
					} else if (robTrainingFinalListing.getStatus()
							.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_acknowledge)) {

						resubmit.setVisible(false);
						query.setVisible(false);
						approve.setVisible(false);
						acknowledge.setVisible(false);
						remark.setVisible(false);
						listQueryView.setVisible(false);
						summaryParticipant.setVisible(false);
					}

				} else if (userListHosComtrac.contains(UserEnvironmentHelper.getLoginName())) {

					if (robTrainingFinalListing.getStatus().equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_approved)) {

						resubmit.setVisible(false);
						query.setVisible(false);
						approve.setVisible(false);
						remark.setVisible(false);
					} else if (robTrainingFinalListing.getStatus()
							.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_pending)) {

						resubmit.setVisible(false);
						acknowledge.setVisible(false);
						downloadPdf.setVisible(false);
						downloadExcel.setVisible(false);
					} else if (robTrainingFinalListing.getStatus()
							.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_query)) {

						query.setVisible(false);
						approve.setVisible(false);
						resubmit.setVisible(false);
						acknowledge.setVisible(false);
						downloadPdf.setVisible(false);
						downloadExcel.setVisible(false);
						remark.setVisible(false);
					} else if (robTrainingFinalListing.getStatus()
							.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_acknowledge)) {

						resubmit.setVisible(false);
						query.setVisible(false);
						approve.setVisible(false);
						acknowledge.setVisible(false);
						remark.setVisible(false);
					}
				}
			}
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
