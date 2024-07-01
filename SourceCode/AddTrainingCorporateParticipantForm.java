package com.ssm.ezbiz.comtrac;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import com.ssm.ezbiz.otcPayment.ViewOtcPaymentPage;
import com.ssm.ezbiz.service.RobParticipantRegTrainingService;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.common.service.LlpParametersService;
import com.ssm.llp.base.exception.SSMException;
import com.ssm.llp.base.page.BaseFrame;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSessionSortableDataProvider;
import com.ssm.llp.base.utils.LlpDateUtils;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMDateTextField;
import com.ssm.llp.base.wicket.component.SSMDropDownChoice;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMRadioChoice;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobParticipantRegTraining;
import com.ssm.llp.ezbiz.model.RobFormA;
import com.ssm.llp.ezbiz.model.RobFormABranches;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.mod1.page.NumberValidator;
import com.ssm.llp.mod1.service.LlpUserProfileService;
import com.ssm.llp.wicket.SSMAjaxFormSubmitBehavior;

@SuppressWarnings({ "all" })
public class AddTrainingCorporateParticipantForm extends BaseFrame {

	@SpringBean(name = "RobTrainingParticipantService")
	private RobTrainingParticipantService robTrainingParticipantService;

	@SpringBean(name = "RobTrainingConfigService")
	private RobTrainingConfigService robTrainingConfigService;

	@SpringBean(name = "RobTrainingTransactionService")
	private RobTrainingTransactionService robTrainingTransactionService;

	@SpringBean(name = "RobParticipantRegTrainingService")
	private RobParticipantRegTrainingService robParticipantRegTrainingService;

	@SpringBean(name = "LlpParametersService")
	LlpParametersService llpParametersService;

	private int participantIdx = -1;
	private boolean canUpdate = true;
	private RobTrainingTransaction robTrainingTransaction;

	public AddTrainingCorporateParticipantForm(final RobTrainingParticipant robTrainingParticipant,
			RobTrainingConfig trainingConfig, final ModalWindow addParticipantPopup) {
		this(robTrainingParticipant, trainingConfig, addParticipantPopup, -1);
	}

	public AddTrainingCorporateParticipantForm(final RobTrainingParticipant robTrainingParticipant,
			RobTrainingConfig trainingConfig, final ModalWindow addParticipantPopup, final int participantIdx) {
		this(robTrainingParticipant, trainingConfig, addParticipantPopup, participantIdx, true, null);
	}

	public AddTrainingCorporateParticipantForm(final RobTrainingParticipant robTrainingParticipant,
			final RobTrainingConfig trainingConfig, final ModalWindow addParticipantPopup, final int participantIdx,
			boolean canUpdate, Page fpage) {
		this.canUpdate = canUpdate;
		this.participantIdx = participantIdx;
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				if (robTrainingParticipant != null) {
					return robTrainingParticipant;
				} else {
					RobTrainingParticipant participant = new RobTrainingParticipant();
//							participant.setFeeType(Parameter.COMTRAC_FEE_TYPE_standard);
//							participant.setAmount(trainingConfig.getStandardFee());
//							participant.setGstAmount(trainingConfig.getStandardFeeGst());
					return participant;
				}

			}
		}));
		add(new RegisterTrainingParticipantForm("corporateParticipantForm", getDefaultModel(), addParticipantPopup,
				trainingConfig, fpage));
	}

//	SSMLabel amount;
//	Double fee = 0.0;
//	Double gst = 0.0;
	String tempIc = null;
	RobTrainingParticipant participantTemp;
	Date tempDob = null;

	private class RegisterTrainingParticipantForm extends Form {

		public RegisterTrainingParticipantForm(String id, IModel m, final ModalWindow addParticipantPopup,
				final RobTrainingConfig trainingConfig, Page fpage) {
			super(id, m);
			setAutoCompleteForm(false);

			final RobTrainingParticipant trainingParticipant = (RobTrainingParticipant) m.getObject();

			participantTemp = copyFrom(participantTemp, trainingParticipant);
			final WebMarkupContainer wmcParticipant = new WebMarkupContainer("wmcParticipant");
			wmcParticipant.setOutputMarkupPlaceholderTag(true);
			wmcParticipant.setOutputMarkupId(true);
			wmcParticipant.setVisible(false);
			add(wmcParticipant);

			if (trainingParticipant.getIcNo() != null) {
				tempIc = trainingParticipant.getIcNo();
			}

			final SSMTextField name = new SSMTextField("name");
			name.setRequired(true);
			name.add(StringValidator.maximumLength(100));
			name.setLabelKey("page.addParticipant.form.lbl.name");
			wmcParticipant.add(name);
			
			final SSMDateTextField dob = new SSMDateTextField("dob", Parameter.DATEFORMATSHORT, true);
			dob.setRequired(true);
			dob.setOutputMarkupId(true);
			dob.setLabelKey("page.addParticipant.form.lbl.dob");
			dob.setEnabled(false);
			wmcParticipant.add(dob);

			SSMTextField idNo = new SSMTextField("icNo");
			idNo.setRequired(true);
			idNo.add(StringValidator.maximumLength(12));
			idNo.setLabelKey("page.addParticipant.form.lbl.icNo");
			idNo.setOutputMarkupId(true);

			final SSMDropDownChoice designation = new SSMDropDownChoice("occupation_code",
			llpParametersService.findByActiveCodeType(Parameter.COMTRAC_DESIGNATION));
			designation.setRequired(true);
			designation.setOutputMarkupId(true);
			designation.setLabelKey("page.addParticipant.form.lbl.designation");
			wmcParticipant.add(designation);
			
			final SSMTextField job_title = new SSMTextField("job_title");
			job_title.add(StringValidator.maximumLength(100));
			job_title.setRequired(true);
			job_title.setLabelKey("page.addParticipant.form.lbl.job_title");
			wmcParticipant.add(job_title);

			final SSMTextField company = new SSMTextField("company");
			company.setRequired(true);
			company.add(StringValidator.maximumLength(100));
			company.setLabelKey("page.addParticipant.form.lbl.company");
			wmcParticipant.add(company);

			final SSMTextField add1 = new SSMTextField("address1");
			add1.add(StringValidator.maximumLength(150));
			add1.setRequired(true);
			add1.setLabelKey("page.addParticipant.form.lbl.address1");
			wmcParticipant.add(add1);

			final SSMTextField add2 = new SSMTextField("address2");
			add2.add(StringValidator.maximumLength(150));
			// add2.setRequired(true);
			wmcParticipant.add(add2);

			final SSMTextField add3 = new SSMTextField("address3");
			// add3.setRequired(true);
			add3.add(StringValidator.maximumLength(150));
			wmcParticipant.add(add3);

			final SSMTextField telNo = new SSMTextField("telNo");
			telNo.add(StringValidator.maximumLength(15));
			telNo.setRequired(true);
			telNo.add(new NumberValidator());
			telNo.setLabelKey("page.addParticipant.form.lbl.telNo");
			wmcParticipant.add(telNo);

			final SSMTextField postcode = new SSMTextField("postcode");
			postcode.setRequired(true);
			postcode.add(StringValidator.maximumLength(5));
			postcode.add(new NumberValidator());
			postcode.setLabelKey("page.addParticipant.form.lbl.postcode");
			wmcParticipant.add(postcode);

			final SSMTextField city = new SSMTextField("city");
			city.setRequired(true);
			city.add(StringValidator.maximumLength(100));
			city.setLabelKey("page.addParticipant.form.lbl.city");
			wmcParticipant.add(city);

			final SSMDropDownChoice state = new SSMDropDownChoice("state", Parameter.CBS_ROB_STATE);
			state.setRequired(true);
			state.setOutputMarkupId(true);
			state.setLabelKey("page.addParticipant.form.lbl.state");
			wmcParticipant.add(state);

			final SSMDropDownChoice diet = new SSMDropDownChoice("diet", Parameter.DIETARY_TYPE);
			diet.setRequired(true);
			diet.setOutputMarkupId(true);
			diet.setLabelKey("page.addParticipant.form.lbl.diet");
			wmcParticipant.add(diet);

			final SSMTextField faxNo = new SSMTextField("faxNo");
			faxNo.add(StringValidator.maximumLength(15));//
			faxNo.setRequired(true);
			faxNo.setLabelKey("page.addParticipant.form.lbl.faxNo");
			faxNo.add(new NumberValidator());
			wmcParticipant.add(faxNo);

			final SSMTextField email = new SSMTextField("email");
			email.setRequired(true);
			email.add(StringValidator.maximumLength(50));
			email.add(EmailAddressValidator.getInstance());
			email.setUpperCase(false);
			email.setLabelKey("page.addParticipant.form.lbl.email");
			wmcParticipant.add(email);

//			final WebMarkupContainer wmcLsNo = new WebMarkupContainer("wmcLsNo");
//			wmcLsNo.setOutputMarkupPlaceholderTag(true);
//			wmcLsNo.setOutputMarkupId(true);
//			wmcParticipant.add(wmcLsNo);

//			final WebMarkupContainer wmsMemberNo = new WebMarkupContainer("wmsMemberNo");
//			wmsMemberNo.setOutputMarkupPlaceholderTag(true);
//			wmsMemberNo.setOutputMarkupId(true);
//			wmcParticipant.add(wmsMemberNo);

//			final SSMTextField lsNo = new SSMTextField("lsNo");
//			lsNo.setRequired(true);
//			lsNo.setOutputMarkupId(true);
//			lsNo.setLabelKey("page.addParticipant.form.lbl.lsNo");
//			wmcLsNo.add(lsNo);

//			final SSMTextField memberNo = new SSMTextField("membershipNo");
//			memberNo.setRequired(true);
//			memberNo.setOutputMarkupId(true);
//			memberNo.setLabelKey("page.addParticipant.form.lbl.membershipNo");
//			wmsMemberNo.add(memberNo);

//			amount = new SSMLabel("amount");
//			amount.setOutputMarkupId(true);
//			wmcParticipant.add(amount);

//			if(Parameter.COMTRAC_FEE_TYPE_standard.equals(trainingParticipant.getFeeType())){
//				wmcLsNo.setVisible(false);
//				wmsMemberNo.setVisible(false);
//				lsNo.setDefaultModelObject("");
//				memberNo.setDefaultModelObject("");
//			}else{
//				if(Parameter.COMTRAC_FEE_TYPE_license_sec.equals(trainingParticipant.getFeeType())){
//					wmcLsNo.setVisible(true);
//					wmsMemberNo.setVisible(false);
//					memberNo.setDefaultModelObject("");
//				}else{
//					wmcLsNo.setVisible(false);
//					wmsMemberNo.setVisible(true);
//					lsNo.setDefaultModelObject("");
//				}
//			}

//			fee = trainingParticipant.getAmount();
//			gst = trainingParticipant.getGstAmount();
//			final SSMRadioChoice feeType = new SSMRadioChoice("feeType", Parameter.COMTRAC_FEE_TYPE);
//			feeType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
//				@Override
//				protected void onUpdate(AjaxRequestTarget target) {
//					RobTrainingParticipant participantObj = (RobTrainingParticipant) getModelObject();
//					trainingParticipant.setFeeType(participantObj.getFeeType());
//					
//					if(Parameter.COMTRAC_FEE_TYPE_standard.equals(participantObj.getFeeType())){
//						fee = trainingConfig.getStandardFee();
//						gst = trainingConfig.getStandardFeeGst();
//						wmcLsNo.setVisible(false);
//						wmsMemberNo.setVisible(false);
//						lsNo.setDefaultModelObject("");
//						memberNo.setDefaultModelObject("");
//					}else{
//						if(Parameter.COMTRAC_FEE_TYPE_license_sec.equals(participantObj.getFeeType())){
//							wmcLsNo.setVisible(true);
//							wmsMemberNo.setVisible(false);
//							memberNo.setDefaultModelObject("");
//						}else{
//							wmcLsNo.setVisible(false);
//							wmsMemberNo.setVisible(true);
//							lsNo.setDefaultModelObject("");
//						}
//						fee = trainingConfig.getSpecialFee();
//						gst = trainingConfig.getSpecialFeeGst();
//					}
//					
//					amount.setDefaultModelObject(fee);
//					
//					target.add(wmsMemberNo);
//					target.add(wmcLsNo);
//					target.add(amount);
//					
//				}
//			});
//			feeType.setOutputMarkupId(false);
//			wmcParticipant.add(feeType);
			
			final SSMRadioChoice gender = new SSMRadioChoice("gender", Parameter.GENDER);
			gender.add(new AjaxFormChoiceComponentUpdatingBehavior() {

				@Override

				protected void onUpdate(AjaxRequestTarget target) {
					RobTrainingParticipant participantObj = (RobTrainingParticipant) getModelObject();
					trainingParticipant.setGender(participantObj.getGender());

				}
			});
			gender.setRequired(true);
			gender.setOutputMarkupId(true);
			wmcParticipant.add(gender);
			
			//form validationJS
			final String formValidationJS = "formValidation";
			String mainFieldToValidate[] = new String[]{"name","idNo","designation","job_title","company","address1","telNo","postcode","city","state","diet","faxNo","email"};
			String mainFieldToValidateRules[] = new String[]{"empty","exactLengthNumber[12]","empty","empty","empty","empty","minLengthNumber[10]","exactLengthNumber[5]","empty","empty","empty","minLengthNumber[8]","email"}; //isNotReqUrl means set field ie. url tak mandatory. Refer form.js dan semantic.js utk validation url/email.
			setSemanticJSValidation(this, formValidationJS, mainFieldToValidate, mainFieldToValidateRules);


			AjaxFormComponentUpdatingBehavior idNoKeyup = new AjaxFormComponentUpdatingBehavior("onkeyup") { // add
																												// validation
																												// check
																												// participant
																												// training
																												// date
																												// conflict
																												// keyin
																												// ic..
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					String ic = (String) getFormComponent().getDefaultModelObject(); // get ic input from form keyin..
					tempDob = null;
					if (!StringUtils.isBlank(ic)) {
						if (ic.length() == 12) {
							Date dobGenerated = LlpDateUtils.generateDobFromIdNo(idNo.getInput(), Parameter.DATEFORMATSHORT);
							dob.setDefaultModelObject(dobGenerated);	
							tempDob = dobGenerated;
							if (ic.equals(tempIc)) { // jika ic adalah sama lodger??
								name.setDefaultModelObject(participantTemp.getName());
								designation.setDefaultModelObject(participantTemp.getDesignation());
								job_title.setDefaultModelObject(participantTemp.getJob_title());
								company.setDefaultModelObject(participantTemp.getCompany());
								telNo.setDefaultModelObject(participantTemp.getTelNo());
								faxNo.setDefaultModelObject(participantTemp.getFaxNo());
								postcode.setDefaultModelObject(participantTemp.getPostcode());
								city.setDefaultModelObject(participantTemp.getCity());
								state.setDefaultModelObject(participantTemp.getState());
								diet.setDefaultModelObject(participantTemp.getDiet());
								add1.setDefaultModelObject(participantTemp.getAddress1());
								add2.setDefaultModelObject(participantTemp.getAddress2());
								add3.setDefaultModelObject(participantTemp.getAddress3());
								email.setDefaultModelObject(participantTemp.getEmail());
								gender.setDefaultModelObject(participantTemp.getGender());
//								lsNo.setDefaultModelObject(participantTemp.getLsNo());
//								memberNo.setDefaultModelObject(participantTemp.getMembershipNo());
//								if(trainingParticipant.getRobTrainingTransaction() != null){
//									if(trainingParticipant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")){
//										feeType.setDefaultModelObject(participantTemp.getFeeType());
//										amount.setDefaultModelObject(participantTemp.getAmount());
//										
//										if(Parameter.COMTRAC_FEE_TYPE_standard.equals(participantTemp.getFeeType())){
//											wmcLsNo.setVisible(false);
//											wmsMemberNo.setVisible(false);
//											lsNo.setDefaultModelObject("");
//											memberNo.setDefaultModelObject("");
//										}else{
//											if(Parameter.COMTRAC_FEE_TYPE_license_sec.equals(participantTemp.getFeeType())){
//												wmcLsNo.setVisible(true);
//												wmsMemberNo.setVisible(false);
//												memberNo.setDefaultModelObject("");
//											}else{
//												wmcLsNo.setVisible(false);
//												wmsMemberNo.setVisible(true);
//												lsNo.setDefaultModelObject("");
//											}
//										}
//										
//										
//									}
//								}
								wmcParticipant.setVisible(true);
							} else {
								List<RobTrainingParticipant> participants = robTrainingParticipantService
										.findAllParticipantByTrainingIdStatus(trainingConfig.getTrainingId(),
												new String[] { Parameter.COMTRAC_TRANSACTION_STATUS_data_entry,
														Parameter.COMTRAC_TRANSACTION_STATUS_payment_success,
														Parameter.COMTRAC_TRANSACTION_STATUS_pending_payment },
												ic);
								if (participants.size() == 0) {
									name.setDefaultModelObject("");
									designation.setDefaultModelObject("");
									job_title.setDefaultModelObject("");
									company.setDefaultModelObject("");
									telNo.setDefaultModelObject("");
									faxNo.setDefaultModelObject("");
									postcode.setDefaultModelObject("");
									city.setDefaultModelObject("");
									state.setDefaultModelObject("");
									gender.setDefaultModelObject("");
									diet.setDefaultModelObject("");
									add1.setDefaultModelObject("");
									add2.setDefaultModelObject("");
									add3.setDefaultModelObject("");
									email.setDefaultModelObject("");
//									lsNo.setDefaultModelObject("");
//									memberNo.setDefaultModelObject("");
//									if(trainingParticipant.getRobTrainingTransaction() != null){
//										if(trainingParticipant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")){
//											feeType.setDefaultModelObject(Parameter.COMTRAC_FEE_TYPE_standard);
//											amount.setDefaultModelObject(trainingConfig.getStandardFee());
//											feeType.setEnabled(false);
//											
//											wmcLsNo.setVisible(false);
//											wmsMemberNo.setVisible(false);
//											lsNo.setDefaultModelObject("");
//											memberNo.setDefaultModelObject("");
//										}
//									}

									wmcParticipant.setVisible(true); // form isi participant..
								} else {
									RobTrainingParticipant participant = participants.get(0);
									ssmError("error.comtrac.participant.alreadyExist", participant.getIcNo(),
											participant.getRobTrainingTransaction().getTrainingId().getTrainingName());
									wmcParticipant.setVisible(false);
								}
							}

							String checkClashTraining = llpParametersService.findByCodeTypeValue(Parameter.LLP_CONFIG,
									Parameter.LLP_CONFIG_CHECK_CLASH_TRAINING_COMTRAC);
							if (Parameter.YES_NO_yes.equals(checkClashTraining)) { // if enable check clash training
																					// date
								Date startDate = trainingConfig.getTrainingStartDt();
								Date endDate = trainingConfig.getTrainingEndDt();
								try {
									List<RobParticipantRegTraining> participantRegisteredTrainingList = robParticipantRegTrainingService
											.findParticipantCurrentTrainingBetweenStartDtEndDt(startDate, endDate, ic);
									if (participantRegisteredTrainingList.size() > 0) { // if found clash training..
										RobParticipantRegTraining participantRegTraining = participantRegisteredTrainingList
												.get(0);
										ssmError("error.comtrac.participant.clashTrainingFound",
												participantRegTraining.getIcNo(), participantRegTraining.getName(),
												participantRegTraining.getTrainingName(),
												participantRegTraining.getTransactionCode(),
												participantRegTraining.getTrainingCode(),
												String.valueOf(participantRegTraining.getTrainingId()));
										wmcParticipant.setVisible(false);
									}

								} catch (SSMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						} // end-if ic 12..

						else {
							wmcParticipant.setVisible(false);
						}
						target.add(wmcParticipant);
						target.add(dob);
						FeedbackPanel feedbackPanel = ((AddTrainingCorporateParticipantForm) getPage())
								.getFeedbackPanel();
						feedbackPanel.getFeedbackMessages().clear();
						target.add(feedbackPanel);
					}
				}
			};
			idNo.add(idNoKeyup);

			if (!(fpage instanceof ViewListParticipantSummary)) {
				if (participantIdx != -1) {
					idNo.setReadOnly(true);
					wmcParticipant.setVisible(true);
				} else {
					idNo.setReadOnly(false);
					wmcParticipant.setVisible(false);
				}

			} else {
				wmcParticipant.setVisible(true);
			}

			add(idNo);

			SSMAjaxButton submitParticipantForm = new SSMAjaxButton("submitParticipantForm", formValidationJS) { // submit participant
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					RobTrainingParticipant participantsubmit = (RobTrainingParticipant) form.getDefaultModelObject();
					List<RobTrainingParticipant> listRobTrainingParticipant = (List<RobTrainingParticipant>) getSession()
							.getAttribute("listParticipant_");
					participantsubmit.setDob(tempDob);

					if (participantIdx == -1) {
						RobTrainingParticipant newParticipant = new RobTrainingParticipant();
						newParticipant.setName(participantsubmit.getName());
						newParticipant.setIcNo(participantsubmit.getIcNo());
						newParticipant.setDob(participantsubmit.getDob());
						newParticipant.setTelNo(participantsubmit.getTelNo());
						newParticipant.setFaxNo(participantsubmit.getFaxNo());
						newParticipant.setEmail(participantsubmit.getEmail());
						//newParticipant.setDesignation(participantsubmit.getDesignation());
						newParticipant.setOccupation_code(participantsubmit.getOccupation_code());
						newParticipant.setJob_title(participantsubmit.getJob_title());
						newParticipant.setCompany(participantsubmit.getCompany());
						newParticipant.setGender(participantsubmit.getGender());
						newParticipant.setAmount(null);
						newParticipant.setGstAmount(null);
						newParticipant.setAddress1(participantsubmit.getAddress1());
						newParticipant.setFeeType(Parameter.COMTRAC_FEE_TYPE_standard);
						newParticipant.setIsAttend(Parameter.YES_NO_no);
						/* newParticipant.setState(Parameter.STATE_CODE_TO_CBS_CODE); */
						if (StringUtils.isNotBlank(participantsubmit.getAddress2())) {
							newParticipant.setAddress2(participantsubmit.getAddress2());
						}
						if (StringUtils.isNotBlank(participantsubmit.getAddress3())) {
							newParticipant.setAddress3(participantsubmit.getAddress3());
						}
						if (StringUtils.isNotBlank(participantsubmit.getPostcode())) {
							newParticipant.setPostcode(participantsubmit.getPostcode());
						}
						if (StringUtils.isNotBlank(participantsubmit.getCity())) {
							newParticipant.setCity(participantsubmit.getCity());
						}
						if (StringUtils.isNotBlank(participantsubmit.getState())) {
							newParticipant.setState(participantsubmit.getState());
						}
						if (StringUtils.isNotBlank(participantsubmit.getDiet())) {
							newParticipant.setDiet(participantsubmit.getDiet());
						}
//						if(StringUtils.isNotBlank(participantsubmit.getLsNo())){
//							newParticipant.setLsNo(participantsubmit.getLsNo());
//						}
//						if(StringUtils.isNotBlank(participantsubmit.getMembershipNo())){
//							newParticipant.setMembershipNo(participantsubmit.getMembershipNo());
//						}

						listRobTrainingParticipant.add(newParticipant);

						RobTrainingConfig tempObject = robTrainingConfigService
								.findById(trainingConfig.getTrainingId());
						Integer currentpax = tempObject.getCurrentPax();
						tempObject.setCurrentPax(currentpax + 1);
						robTrainingConfigService.update(tempObject);

					} else {
						RobTrainingParticipant selectedParticipant = listRobTrainingParticipant.get(participantIdx);
						selectedParticipant.setName(participantsubmit.getName());
						selectedParticipant.setIcNo(participantsubmit.getIcNo());
						selectedParticipant.setDob(participantsubmit.getDob());
						selectedParticipant.setTelNo(participantsubmit.getTelNo());
						selectedParticipant.setFaxNo(participantsubmit.getFaxNo());
						selectedParticipant.setEmail(participantsubmit.getEmail());
						//selectedParticipant.setDesignation(participantsubmit.getDesignation());
						selectedParticipant.setOccupation_code(participantsubmit.getOccupation_code());
						selectedParticipant.setJob_title(participantsubmit.getJob_title());
						selectedParticipant.setCompany(participantsubmit.getCompany());
						selectedParticipant.setGender(participantsubmit.getGender());
//						selectedParticipant.setAmount(fee);
//						selectedParticipant.setGstAmount(gst);
						selectedParticipant.setAddress1(participantsubmit.getAddress1());
						

						if (!Parameter.YES_NO_yes.equals(selectedParticipant.getIsAttend())) {
							selectedParticipant.setIsAttend(Parameter.YES_NO_no);
						}

						if (StringUtils.isNotBlank(participantsubmit.getAddress2())) {
							selectedParticipant.setAddress2(participantsubmit.getAddress2());
						}
						if (StringUtils.isNotBlank(participantsubmit.getAddress3())) {
							selectedParticipant.setAddress3(participantsubmit.getAddress3());
						}
						if (StringUtils.isNotBlank(participantsubmit.getPostcode())) {
							selectedParticipant.setPostcode(participantsubmit.getPostcode());
						}
						if (StringUtils.isNotBlank(participantsubmit.getCity())) {
							selectedParticipant.setCity(participantsubmit.getCity());
						}
						if (StringUtils.isNotBlank(participantsubmit.getDiet())) {
							selectedParticipant.setDiet(participantsubmit.getDiet());
						}
						if (StringUtils.isNotBlank(participantsubmit.getState())) {
							selectedParticipant.setState(participantsubmit.getState());
						}
//						if(StringUtils.isNotBlank(participantsubmit.getLsNo())){
//							selectedParticipant.setLsNo(participantsubmit.getLsNo());
//						}
//						if(StringUtils.isNotBlank(participantsubmit.getMembershipNo())){
//							selectedParticipant.setMembershipNo(participantsubmit.getMembershipNo());
//						}

						listRobTrainingParticipant.set(participantIdx, selectedParticipant);
					}

					participantIdx = -1;

					getSession().setAttribute("listParticipant_", (Serializable) listRobTrainingParticipant);
					addParticipantPopup.close(target);

				}
			};
			submitParticipantForm.setOutputMarkupId(true);
			wmcParticipant.add(submitParticipantForm);

			SSMAjaxButton submitParticipantSummary = new SSMAjaxButton("submitParticipantSummary") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					RobTrainingParticipant participantsubmit = (RobTrainingParticipant) form.getDefaultModelObject();

					RobTrainingParticipant selectedParticipant = robTrainingParticipantService
							.findById(trainingParticipant.getParticipantId());
					RobTrainingTransaction robTrainingTransaction = robTrainingTransactionService.findByTransactionCode(
							selectedParticipant.getRobTrainingTransaction().getTransactionCode());

//						robTrainingTransaction.setAmount((robTrainingTransaction.getAmount() - selectedParticipant.getAmount()) + fee);
//						robTrainingTransaction.setGstAmount(robTrainingTransaction.getGstAmount() - selectedParticipant.getGstAmount() + gst);
					robTrainingTransactionService.update(robTrainingTransaction);

					selectedParticipant.setName(participantsubmit.getName());
					selectedParticipant.setIcNo(participantsubmit.getIcNo());
					selectedParticipant.setDob(participantsubmit.getDob());
					selectedParticipant.setTelNo(participantsubmit.getTelNo());
					selectedParticipant.setFaxNo(participantsubmit.getFaxNo());
					selectedParticipant.setEmail(participantsubmit.getEmail());
					//selectedParticipant.setDesignation(participantsubmit.getDesignation());
					selectedParticipant.setOccupation_code(participantsubmit.getOccupation_code());
					selectedParticipant.setJob_title(participantsubmit.getJob_title());
					selectedParticipant.setCompany(participantsubmit.getCompany());
					selectedParticipant.setGender(participantsubmit.getGender());
//						selectedParticipant.setAmount(fee);
//						selectedParticipant.setGstAmount(gst);
					selectedParticipant.setAddress1(participantsubmit.getAddress1());

					if (!Parameter.YES_NO_yes.equals(selectedParticipant.getIsAttend())) {
						selectedParticipant.setIsAttend(Parameter.YES_NO_no);
					}

					if (StringUtils.isNotBlank(participantsubmit.getAddress2())) {
						selectedParticipant.setAddress2(participantsubmit.getAddress2());
					}
					if (StringUtils.isNotBlank(participantsubmit.getAddress3())) {
						selectedParticipant.setAddress3(participantsubmit.getAddress3());
					}
					if (StringUtils.isNotBlank(participantsubmit.getPostcode())) {
						selectedParticipant.setPostcode(participantsubmit.getPostcode());
					}
					if (StringUtils.isNotBlank(participantsubmit.getCity())) {
						selectedParticipant.setCity(participantsubmit.getCity());
					}
					if (StringUtils.isNotBlank(participantsubmit.getState())) {
						selectedParticipant.setState(participantsubmit.getState());
					}
					if (StringUtils.isNotBlank(participantsubmit.getDiet())) {
						selectedParticipant.setDiet(participantsubmit.getDiet());
					}
//						if(StringUtils.isNotBlank(participantsubmit.getLsNo())){
//							selectedParticipant.setLsNo(participantsubmit.getLsNo());
//						}
//						if(StringUtils.isNotBlank(participantsubmit.getMembershipNo())){
//							selectedParticipant.setMembershipNo(participantsubmit.getMembershipNo());
//						}

					robTrainingParticipantService.update(selectedParticipant);

					addParticipantPopup.close(target);

				}
			};
			submitParticipantSummary.setOutputMarkupId(true);
			wmcParticipant.add(submitParticipantSummary);

			if (fpage instanceof ViewListParticipantSummary) {
				submitParticipantForm.setVisible(false);
				submitParticipantSummary.setVisible(true);
				if (trainingParticipant.getRobTrainingTransaction().getLodgerId().equals("SSM STAF")) {
//					feeType.setEnabled(false);
				} else {
//					feeType.setEnabled(false);
				}

			} else {
				submitParticipantForm.setVisible(true);
				submitParticipantSummary.setVisible(false);
//				feeType.setEnabled(false);
			}

		}
	}

	public RobTrainingParticipant copyFrom(RobTrainingParticipant to, RobTrainingParticipant from) {

		to = new RobTrainingParticipant();

		to.setName(from.getName());
		to.setDob(from.getDob());
		to.setEmail(from.getEmail());
		to.setGender(from.getGender());
		to.setTelNo(from.getTelNo());
		to.setFaxNo(from.getFaxNo());
		to.setPostcode(from.getPostcode());
		to.setCity(from.getCity());
		to.setState(from.getState());
		to.setDiet(from.getDiet());
		to.setAddress1(from.getAddress1());
		to.setAddress2(from.getAddress2());
		to.setAddress3(from.getAddress3());
//		to.setAmount(from.getAmount());
		//to.setDesignation(from.getDesignation());
		to.setOccupation_code(from.getOccupation_code());
		to.setJob_title(from.getJob_title());
		to.setCompany(from.getCompany());
//		to.setFeeType(from.getFeeType());
//		to.setLsNo(from.getLsNo());
//		to.setMembershipNo(from.getMembershipNo());

		return to;
	}

	@Override
	public String getPageTitle() {
		// TODO Auto-generated method stub
		return null;
	}
}
