package com.ssm.ezbiz.comtrac;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ssm.ezbiz.comtrac.ListComtracTraining.SearchTrainingModel;
import com.ssm.ezbiz.comtrac.SelectComtracTraining.ComtracFormModel;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingReprintcertService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.db.SearchCriteria;
import com.ssm.llp.base.common.model.LlpPaymentFee;
import com.ssm.llp.base.common.model.LlpPaymentTransactionDetail;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.common.service.LlpPaymentFeeService;
import com.ssm.llp.base.common.service.MailService;
import com.ssm.llp.base.page.PaymentDetailPage;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.ezbiz.model.RobTrainingReprintcert;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.mod1.page.RobTrainingReprintcertList;
import com.ssm.llp.mod1.service.LlpUserProfileService;

@SuppressWarnings({ "all" })
public class RobTrainingReprintcertDetails extends SecBasePage {
	
	@SpringBean(name = "RobTrainingParticipantService")
	RobTrainingParticipantService robTrainingParticipantService;
	
	@SpringBean(name = "RobTrainingTransactionService")
	RobTrainingTransactionService robTrainingTransactionService;
	
	@SpringBean(name = "RobTrainingReprintcertService")
	RobTrainingReprintcertService robTrainingReprintcertService;
	
	@SpringBean(name = "LlpUserProfileService")
	LlpUserProfileService llpUserProfileService;
	
	@SpringBean(name = "LlpPaymentFeeService")
	private LlpPaymentFeeService llpPaymentFeeService;
	
	SSMAjaxButton submitPayment;
	SSMAjaxButton back;
	boolean isInternalUser;
	
	public RobTrainingReprintcertDetails(final RobTrainingParticipant robTrainingParticipant, final RobTrainingReprintcert robTrainingReprintcert, String status, final Page fromPage, String certRefNo) {

				
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {

				RobTrainingParticipant robTrainingParticipants = robTrainingParticipant;
						
				if(robTrainingReprintcert != null){
					try {
						robTrainingParticipants = robTrainingParticipantService.findByTransactionCodeIcNoLodger(robTrainingReprintcert.getTransactionCode(), robTrainingReprintcert.getIcNo(), null);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
					return robTrainingParticipants;
				}else if(robTrainingParticipants != null){
					return robTrainingParticipants;
				}else{
					return null;
				}
				
			}
		}));

		add(new RobTrainingReprintcertDetailsForm("formCert", (IModel<RobTrainingParticipant>) getDefaultModel(), fromPage, status, certRefNo));
	}

	private class RobTrainingReprintcertDetailsForm extends Form implements Serializable{
		
		public RobTrainingReprintcertDetailsForm(String id, IModel<RobTrainingParticipant> m, final Page fromPage, String status, String certRefNo) {
			super(id, m);
			isInternalUser =UserEnvironmentHelper.isInternalUser();
			RobTrainingParticipant robTrainingParticipant = (RobTrainingParticipant) m.getObject();
			
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			
			System.out.println("fromPage ="+fromPage);
			
			String lodgerName = UserEnvironmentHelper.getLoginName();
			Double fee = 0.0;
			Double gst = 0.0;
			
			try {
				
			LlpUserProfile llpUserProfile = llpUserProfileService.findProfileInfoByUserId(lodgerName);
			RobTrainingTransaction robTrainingTransaction = robTrainingTransactionService.findByTransactionCode(robTrainingParticipant.getRobTrainingTransaction().getTransactionCode());
			
			
			add(new SSMLabel("transactionCodes", robTrainingTransaction.getTransactionCode()));
			add(new SSMLabel("trainingCode", robTrainingTransaction.getTrainingId().getTrainingCode()));
			add(new SSMLabel("trainingName", robTrainingTransaction.getTrainingId().getTrainingName()));
			add(new SSMLabel("trainingStartDt", robTrainingTransaction.getTrainingId().getTrainingStartDt()));
			add(new SSMLabel("trainingEndDt", robTrainingTransaction.getTrainingId().getTrainingEndDt()));
			add(new SSMLabel("trainingStartTime", robTrainingTransaction.getTrainingId().getTrainingStartTime()));
			add(new SSMLabel("trainingEndTime", robTrainingTransaction.getTrainingId().getTrainingEndTime()));
			add(new SSMLabel("cpePoint", robTrainingTransaction.getTrainingId().getCpePoint()));
			add(new SSMLabel("trainingVenue", robTrainingTransaction.getTrainingId().getTrainingVenue()));
			
			
			add(new SSMLabel("name", robTrainingParticipant.getName()));
			add(new SSMLabel("icNos", robTrainingParticipant.getIcNo()));
			add(new SSMLabel("gender", robTrainingParticipant.getGender(), Parameter.GENDER));
			add(new SSMLabel("dob", robTrainingParticipant.getDob()));
			add(new SSMLabel("telNo", robTrainingParticipant.getTelNo()));
			add(new SSMLabel("faxNo", robTrainingParticipant.getFaxNo()));
			add(new SSMLabel("email", robTrainingParticipant.getEmail()));
			add(new SSMLabel("company", robTrainingParticipant.getCompany()));
			add(new SSMLabel("occupation", robTrainingParticipant.getOccupation_code(), Parameter.COMTRAC_DESIGNATION));
			add(new SSMLabel("designation", robTrainingParticipant.getJob_title()));
			
			
			
			add(new SSMLabel("address1", robTrainingParticipant.getAddress1()));
			add(new SSMLabel("address2", robTrainingParticipant.getAddress2()));
			add(new SSMLabel("address3", robTrainingParticipant.getAddress3()));
			add(new SSMLabel("postcode", robTrainingParticipant.getPostcode()));
			add(new SSMLabel("city", robTrainingParticipant.getCity()));
			add(new SSMLabel("state", robTrainingParticipant.getState()));
			add(new SSMLabel("diet", robTrainingParticipant.getDiet()));
			add(new SSMLabel("lsNo", robTrainingParticipant.getLsNo()));
			add(new SSMLabel("membershipNo", robTrainingParticipant.getMembershipNo()));
			
			
			 submitPayment = new SSMAjaxButton("submitPayment") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

					RobTrainingReprintcert robTrainingReprintcert = new RobTrainingReprintcert();
					RobTrainingReprintcert robTrainingReprintcertstatus = new RobTrainingReprintcert();
					LlpPaymentFee llpPaymentFee = llpPaymentFeeService.findById(Parameter.COMTRAC_FEE_TYPE_reprint_cert);
					
					try {
					robTrainingReprintcertstatus = robTrainingReprintcertService.findBycertRefNo(certRefNo);
					
					if(robTrainingReprintcertstatus == null) {
					robTrainingReprintcert.setTransactionCode(robTrainingTransaction.getTransactionCode());
					robTrainingReprintcert.setTrainingCode(robTrainingTransaction.getTrainingId().getTrainingCode());
					robTrainingReprintcert.setTrainingName(robTrainingTransaction.getTrainingId().getTrainingName());
					robTrainingReprintcert.setIcNo(robTrainingParticipant.getIcNo());
					robTrainingReprintcert.setStatus("DE");
					robTrainingReprintcert.setAmount(llpPaymentFee.getPaymentFee());
					robTrainingReprintcert.setGstAmount(gst);
					robTrainingReprintcert.setLodgerId(llpUserProfile.getLoginId());
					robTrainingReprintcert.setLodgerName(lodgerName);
					robTrainingReprintcert.setPaymentChannel("ezbiz");
					robTrainingReprintcert.setRegistrationChannel("ezbiz");
					}else {
						
						robTrainingReprintcert = robTrainingReprintcertstatus;
					}
					
						robTrainingReprintcertService.updateInsertAll(robTrainingReprintcert);	
										
					
					LlpPaymentTransactionDetail paymentItem = new LlpPaymentTransactionDetail();
					List<LlpPaymentTransactionDetail> listPaymentItems = new ArrayList<LlpPaymentTransactionDetail>();
					
				
				        paymentItem.setPaymentItem(Parameter.COMTRAC_FEE_TYPE_reprint_cert);
						paymentItem.setQuantity(1);
						paymentItem.setPaymentDet(lodgerName + " ("+llpUserProfile.getIdNo()+")");
						paymentItem.setAmount(llpPaymentFee.getPaymentFee());
						paymentItem.setGstAmt(gst);
						paymentItem.setGstCode(Parameter.GST_CODE_TYPE);
						listPaymentItems.add(paymentItem);

					setResponsePage(new PaymentDetailPage(robTrainingReprintcert.getCertRefNo(),
							RobTrainingReprintcertService.class.getSimpleName(), robTrainingReprintcert,
							listPaymentItems));
					
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

				}
			};
			
			add(submitPayment);
			
			if(status.equals("PS") || isInternalUser || status.equals("CR" ) || status.equals("C" )) {
				submitPayment.setVisible(false);
			}else {
				submitPayment.setVisible(true);
			}
			
			back = new SSMAjaxButton("back") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					setResponsePage(fromPage);
				}
			};
			add(back);
			
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	}
		
	public String getPageTitle() {
		String titleKey = "page.lbl.ecomtrac.printCert";
		return titleKey;
	}
}