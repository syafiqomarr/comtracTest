package com.ssm.ezbiz.comtrac;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;

import com.ssm.ezbiz.comtrac.ListComtracTraining.SearchTrainingModel;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingReprintcertService;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.ezbiz.model.RobTrainingReprintcert;

@SuppressWarnings({"all"})
public class RobTrainingReprintcertSearch extends SecBasePage {

	@SpringBean(name = "RobTrainingParticipantService")
	private RobTrainingParticipantService robTrainingParticipantService;
	
	@SpringBean(name = "RobTrainingReprintcertService")
	private RobTrainingReprintcertService robTrainingReprintcertService;
	
	WebMarkupContainer errorSearch;
	
	public RobTrainingReprintcertSearch() {
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				SearchTrainingModel searchTrainingModel = new SearchTrainingModel();
				return searchTrainingModel;
			}
		}));
		add(new SearchForm("searchPanel", (IModel<SearchTrainingModel>) getDefaultModel()));
	}
	
	private class SearchForm extends Form implements Serializable {
		public SearchForm(String id, IModel<SearchTrainingModel> m) {
			super(id, m);
			
			errorSearch = new WebMarkupContainer("errorSearch");
			errorSearch.setOutputMarkupPlaceholderTag(true);
			errorSearch.setOutputMarkupId(true);
			errorSearch.setVisible(false);
			add(errorSearch);
			
			SSMTextField transactionCode = new SSMTextField("transactionCode");
			transactionCode.setRequired(true);
			transactionCode.add(StringValidator.maximumLength(100));
			transactionCode.setLabelKey("page.lbl.ecomtrac.transactionCode");
			add(transactionCode);
			
			SSMTextField icNo = new SSMTextField("icNo");
			icNo.setRequired(true);
			icNo.add(StringValidator.maximumLength(100));
			icNo.setLabelKey("page.lbl.ecomtrac.icNo");
			add(icNo);		
			
						
		//form validationJS
		final String formValidationJS = "formValidation";
		String mainFieldToValidate[] = new String[]{"transactionCode","icNo"};
		String mainFieldToValidateRules[] = new String[]{"empty","exactLengthNumber[12]"}; //isNotReqUrl means set field ie. url tak mandatory. Refer form.js dan semantic.js utk validation url/email.
		setSemanticJSValidation(this, formValidationJS, mainFieldToValidate, mainFieldToValidateRules);
		
		
		
		SSMAjaxButton search = new SSMAjaxButton("search", formValidationJS) {
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String lodgerName = UserEnvironmentHelper.getLoginName();
				SearchTrainingModel searchTrainingModel = (SearchTrainingModel) form.getDefaultModelObject();
			try {
			  
			RobTrainingParticipant robTrainingParticipant = robTrainingParticipantService.findByTransactionCodeIcNoLodger(searchTrainingModel.getTransactionCode(), searchTrainingModel.getIcNo(), lodgerName);
			String errorKey = "page.lbl.ecomtrac.notFound";
			
			RobTrainingReprintcert robTrainingReprintcert = robTrainingReprintcertService.findDrafTransaction(searchTrainingModel.getTransactionCode(), searchTrainingModel.getIcNo(), lodgerName);
			
			if(robTrainingParticipant==null){
				errorSearch.setVisible(true);
				
			} else{
				if(robTrainingReprintcert==null){
				setResponsePage(new RobTrainingReprintcertDetails(robTrainingParticipant, null,"NW",getPage(), null));
				}else {
				setResponsePage(new RobTrainingReprintcertDetails(robTrainingParticipant, null,"NW",getPage(), robTrainingReprintcert.getCertRefNo()));
				}
			}
			}
			catch (Exception e) {
				error(e);
			}
			target.add(errorSearch);
			}
		 };
		 add(search);

	}
	}
	
	public class SearchTrainingModel implements Serializable{
		private String icNo;
		private String transactionCode;
		
		public String getTransactionCode() {
			return transactionCode;
		}

		public void setTransactionCode(String transactionCode) {
			this.transactionCode = transactionCode;
		}

		public String getIcNo() {
			return icNo;
		}

		public void setIcNo(String icNo) {
			this.icNo = icNo;
		}
	}

	@Override
	public String getPageTitle() {
		return "page.lbl.ecomtrac.printCert";
	}
}
	
