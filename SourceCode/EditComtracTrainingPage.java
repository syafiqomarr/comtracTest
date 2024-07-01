package com.ssm.ezbiz.comtrac;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.ssm.ezbiz.comtrac.ListComtracTraining.ListComtracTrainingForm;
import com.ssm.ezbiz.comtrac.ListComtracTraining.SearchTrainingModel;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.model.LlpParameters;
import com.ssm.llp.base.common.model.LlpPaymentFee;
import com.ssm.llp.base.common.service.LlpParametersService;
import com.ssm.llp.base.common.service.LlpPaymentFeeService;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.page.WicketApplication;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMAjaxCheckBox;
import com.ssm.llp.base.wicket.component.SSMDateTextField;
import com.ssm.llp.base.wicket.component.SSMDropDownChoice;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextArea;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobRenewal;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;

@SuppressWarnings({"all"})
public class EditComtracTrainingPage extends SecBasePage{

	@SpringBean(name="RobTrainingConfigService")
	RobTrainingConfigService robTrainingConfigService;
	
	@SpringBean(name="LlpPaymentFeeService")
	LlpPaymentFeeService llpPaymentFeeService;
	
	@SpringBean(name="LlpParametersService")
	LlpParametersService llpParametersService;
	
	public EditComtracTrainingPage() {
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				RobTrainingConfig robTrainingConfig = new RobTrainingConfig();
				robTrainingConfig.setIsActive(Boolean.TRUE);
				return robTrainingConfig;
			}
		}));
		add(new EditComtracTrainingPageForm("form", (IModel<RobTrainingConfig>) getDefaultModel()));
		
		insertTinyMce();
	}
	
	public EditComtracTrainingPage(final RobTrainingConfig robTrainingConfig) {
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				
				//remove prefix for user view
				//System.out.println("indexof value1: "+Parameter.COMTRAC_prefix.indexOf(robTrainingConfig.getTrainingCode())); //test return -1
				//System.out.println("indexof value2: "+robTrainingConfig.getTrainingCode().indexOf(Parameter.COMTRAC_prefix)); //test return 0
				if (robTrainingConfig.getTrainingCode().indexOf(Parameter.COMTRAC_prefix)!=-1){ //if contains hardcoded "CP-"
				String trainingCode = robTrainingConfig.getTrainingCode().substring(Parameter.COMTRAC_prefix.length()); //length = 3
				robTrainingConfig.setTrainingCode(trainingCode);
				}	
				return robTrainingConfig;
			}
		}));
		add(new EditComtracTrainingPageForm("form", (IModel<RobTrainingConfig>) getDefaultModel()));
		
		insertTinyMce();
	}
	
	public void insertTinyMce(){
		String contextPath = WicketApplication.get().getServletContext().getServletContextName();
		String js ="<script src=\"" + contextPath  + "/tinymce/tinymce.min.js\"></script>";
		Label jsScript = new Label("jsScript", js);
		jsScript.setEscapeModelStrings(false);
		jsScript.setOutputMarkupId(true);
		
		add(jsScript);
	}
	
	Boolean isNew = true;
	
	public class EditComtracTrainingPageForm extends Form implements Serializable{
		public EditComtracTrainingPageForm(String id, IModel<RobTrainingConfig> m){
			super(id, m);
			final RobTrainingConfig robTrainingConfig = m.getObject();
			
			if(robTrainingConfig.getTrainingCode() != null){
				isNew = false;
			}
			
			SSMTextField trainingCode = new SSMTextField("trainingCode");
			trainingCode.setLabelKey("page.lbl.ezbiz.editTraining.trainingCode");
			trainingCode.setRequired(true);
			add(trainingCode);
			
			SSMTextField trainingName = new SSMTextField("trainingName");
			trainingName.setLabelKey("page.lbl.ezbiz.editTraining.trainingName");
			trainingName.setRequired(true);
			add(trainingName);
			
			SSMDropDownChoice trainingType = new SSMDropDownChoice("trainingType", Parameter.COMTRAC_TRAINING_TYPE);
			trainingType.setLabelKey("page.lbl.ecomtrac.editTraining.trainingType");
			trainingType.setRequired(true);
			add(trainingType);
			
			SSMDateTextField trainingStartDt = new SSMDateTextField("trainingStartDt");
			trainingStartDt.setLabelKey("page.lbl.ezbiz.editTraining.trainingStartDt");
			trainingStartDt.setRequired(true);
			add(trainingStartDt);

			SSMDateTextField trainingEndDt = new SSMDateTextField("trainingEndDt");
			trainingEndDt.setLabelKey("page.lbl.ezbiz.editTraining.trainingEndDt");
			trainingEndDt.setRequired(true);
			add(trainingEndDt);
			
			SSMDateTextField regClosingDt = new SSMDateTextField("regClosingDt");
			regClosingDt.setLabelKey("page.lbl.ezbiz.editTraining.regClosingDt");
			regClosingDt.setRequired(true);
			add(regClosingDt);
			
			SSMTextField trainingStartTime = new SSMTextField("trainingStartTime");
			trainingStartTime.setLabelKey("page.lbl.ezbiz.editTraining.trainingStartTime");
			trainingStartTime.setRequired(true);
			add(trainingStartTime);
			
			SSMTextField trainingEndTime = new SSMTextField("trainingEndTime");
			trainingEndTime.setLabelKey("page.lbl.ezbiz.editTraining.trainingEndTime");
			trainingEndTime.setRequired(true);
			add(trainingEndTime);
			
			SSMTextField cpePoint = new SSMTextField("cpePoint");
			cpePoint.setLabelKey("page.lbl.ezbiz.editTraining.cpePoint");
			cpePoint.setRequired(true);
			add(cpePoint);
			
			SSMTextField trainingVenue = new SSMTextField("trainingVenue");
			trainingVenue.setLabelKey("page.lbl.ezbiz.editTraining.trainingVenue");
			trainingVenue.setRequired(true);
			add(trainingVenue);
			
			SSMTextField standardFee = new SSMTextField("standardFee");
			standardFee.setLabelKey("page.lbl.ezbiz.editTraining.standardFee");
			standardFee.setRequired(true);
			add(standardFee);
			
			SSMTextField specialFee = new SSMTextField("specialFee");
			specialFee.setLabelKey("page.lbl.ezbiz.editTraining.specialFee");
			specialFee.setRequired(true);
			add(specialFee);
			
			SSMTextField maxPax = new SSMTextField("maxPax");
			maxPax.setLabelKey("page.lbl.ezbiz.editTraining.maxPax");
			maxPax.setRequired(true);
			add(maxPax);
			
			SSMTextField trainingListSeq = new SSMTextField("trainingListSeq");
			trainingListSeq.setLabelKey("page.lbl.ezbiz.editTraining.trainingListSeq");
			add(trainingListSeq);
			
			SSMDropDownChoice gstCode = new SSMDropDownChoice("gstCode", Parameter.GST_CODE_TYPE);
			gstCode.setLabelKey("page.lbl.ezbiz.editTraining.gstCode");
			add(gstCode);
			
			final SSMTextArea trainingDesc = new SSMTextArea("trainingDesc");
			add(trainingDesc);
			
			final SSMLabel statusLabel = new SSMLabel("statusLabel", robTrainingConfig.getIsActive().toString());
			statusLabel.setOutputMarkupId(true);
			add(statusLabel);
			
			System.out.println("isNew : " + isNew);
			if(isNew){
				trainingCode.setReadOnly(false);
			}else{
				trainingCode.setReadOnly(true);
			}
			
			AjaxCheckBox status = new AjaxCheckBox("status",  new PropertyModel(robTrainingConfig, "isActive")) {
				@Override
				protected void onUpdate(AjaxRequestTarget arg0) {
					RobTrainingConfig obj = (RobTrainingConfig) getForm().getDefaultModelObject();
					robTrainingConfig.setIsActive(Boolean.valueOf(getValue()));
					
					statusLabel.setDefaultModelObject(obj.getIsActive());
					arg0.add(statusLabel);
				}
			};
			add(status);
			Button submit = new Button("submit") {
				@Override
				public void onSubmit() {
					RobTrainingConfig trainingConfig = (RobTrainingConfig) getForm().getDefaultModelObject();
					trainingConfig.setIsActive(robTrainingConfig.getIsActive());
					Double standardFeeWithoutGst = trainingConfig.getStandardFeeWithoutGst();
					Double specialFeeWithoutGst = trainingConfig.getSpecialFeeWithoutGst();
					Double standardFeeGst = trainingConfig.getStandardFee() - standardFeeWithoutGst;
					Double specialFeeGst = trainingConfig.getSpecialFee() - specialFeeWithoutGst;
					
					trainingConfig.setStandardFee(trainingConfig.getStandardFee());
					trainingConfig.setSpecialFee(trainingConfig.getSpecialFee());
					trainingConfig.setStandardFeeGst(standardFeeGst);
					trainingConfig.setSpecialFeeGst(specialFeeGst);
					trainingConfig.setTrainingCode(Parameter.COMTRAC_prefix + trainingConfig.getTrainingCode());
					if((trainingConfig.getTrainingListSeq()!=null)) {
						trainingConfig.setTrainingListSeq(trainingConfig.getTrainingListSeq()); //dropdown list sequence..
					}else {
						trainingConfig.setTrainingListSeq(9999); //default seq..
					}
					
					if(isNew){
						trainingConfig.setCurrentPax(0);
						robTrainingConfigService.insert(trainingConfig);
						
						LlpPaymentFee llpPaymentFeeStan = new LlpPaymentFee();
						llpPaymentFeeStan.setPaymentFee(trainingConfig.getStandardFeeWithoutGst());
						llpPaymentFeeStan.setPaymentCode(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_standard);
						llpPaymentFeeStan.setGstCode(Parameter.PAYMENT_GST_CODE_SR);
						llpPaymentFeeStan.setStatus("A");
						llpPaymentFeeService.insertWithParameter(llpPaymentFeeStan, trainingConfig.getTrainingCode() + " - Standard Fee");
						
						LlpPaymentFee llpPaymentFeeSpec = new LlpPaymentFee();
						llpPaymentFeeSpec.setPaymentFee(trainingConfig.getSpecialFeeWithoutGst());
						llpPaymentFeeSpec.setPaymentCode(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_special);
						llpPaymentFeeSpec.setGstCode(Parameter.PAYMENT_GST_CODE_SR);
						llpPaymentFeeSpec.setStatus("A");
						llpPaymentFeeService.insertWithParameter(llpPaymentFeeSpec, trainingConfig.getTrainingCode() + " - Special Fee");
						
						LlpParameters gafAccCodeStan = new LlpParameters();
						gafAccCodeStan.setCode(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_standard);
						gafAccCodeStan.setCodeDesc(getCodeTypeWithValue(Parameter.GAF_CONFIG, Parameter.GAF_CONFIG_GAF_COMTRAC_ACC_CODE));
						gafAccCodeStan.setCodeType(Parameter.GAF_CONFIG_ACC_CODE_CR);
						gafAccCodeStan.setStatus("A");
						llpParametersService.insert(gafAccCodeStan);
						
						LlpParameters gafAccCodeSpec = new LlpParameters();
						gafAccCodeSpec.setCode(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_special);
						gafAccCodeSpec.setCodeDesc(getCodeTypeWithValue(Parameter.GAF_CONFIG, Parameter.GAF_CONFIG_GAF_COMTRAC_ACC_CODE));
						gafAccCodeSpec.setCodeType(Parameter.GAF_CONFIG_ACC_CODE_CR);
						gafAccCodeSpec.setStatus("A");
						llpParametersService.insert(gafAccCodeSpec);
						
						LlpParameters gafDebitCodeStan = new LlpParameters();
						gafDebitCodeStan.setCode(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_standard);
						gafDebitCodeStan.setCodeDesc("NOT CONFIG YET");
						gafDebitCodeStan.setCodeType(Parameter.GAF_CONFIG_ACC_CODE_DEBIT);
						gafDebitCodeStan.setStatus("A");
						llpParametersService.insert(gafDebitCodeStan);
						
						LlpParameters gafDebitCodeSpec = new LlpParameters();
						gafDebitCodeSpec.setCode(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_special);
						gafDebitCodeSpec.setCodeDesc("NOT CONFIG YET");
						gafDebitCodeSpec.setCodeType(Parameter.GAF_CONFIG_ACC_CODE_DEBIT);
						gafDebitCodeSpec.setStatus("A");
						llpParametersService.insert(gafDebitCodeSpec);
						
					}else{
						robTrainingConfigService.update(trainingConfig);
						LlpPaymentFee paymentFeeStan = llpPaymentFeeService.findById(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_standard);
						paymentFeeStan.setPaymentFee(standardFeeWithoutGst);
						llpPaymentFeeService.update(paymentFeeStan);
						
						LlpPaymentFee paymentFeeSpec = llpPaymentFeeService.findById(trainingConfig.getTrainingCode() + "_" + Parameter.COMTRAC_FEE_TYPE_special);
						paymentFeeSpec.setPaymentFee(specialFeeWithoutGst);
						llpPaymentFeeService.update(paymentFeeSpec);
						
					}
					setResponsePage(ListComtracTraining.class);
				}
			};
			add(submit);
			
			Link cancel = new Link("cancel") {
				@Override
				public void onClick() {
					setResponsePage(ListComtracTraining.class);
				}
			};
			add(cancel);
		}
	}
	
	@Override
	public String getPageTitle() {
		return "page.lbl.ezbiz.editTraining.editAddTraining";
	}
}
