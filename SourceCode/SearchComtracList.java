package com.ssm.ezbiz.comtrac;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.ssm.ezbiz.comtrac.ListComtracTraining.SearchTrainingModel;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.db.SearchCriteria;
import com.ssm.llp.base.page.SSMPagingNavigator;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSessionSortableDataProvider;
import com.ssm.llp.base.page.table.SSMSortableDataProvider;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMDateTextField;
import com.ssm.llp.base.wicket.component.SSMDropDownChoice;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;

@SuppressWarnings({"all"})
public class SearchComtracList extends SecBasePage {

	@SpringBean(name = "RobTrainingTransactionService")
	RobTrainingTransactionService robTrainingTransactionService;
	
	public SearchComtracList() {
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				SearchComtracModel searchComtracModel = new SearchComtracModel();
				return searchComtracModel;
			}
		}));
		add(new SearchComtracForm("form", getDefaultModel()));
	}
	
	SimpleDateFormat fom = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat pars = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public class SearchComtracForm  extends Form implements Serializable{
		public SearchComtracForm(String id, IModel m){
			super(id, m);
			
			SearchComtracModel searchComtracModel = (SearchComtracModel) m.getObject();
			
			SSMTextField transactionNo = new SSMTextField("transactionNo");
			transactionNo.setLabelKey("page.lbl.ezbiz.searchComtracList.transactionNo");
			add(transactionNo);
			
			SSMTextField lodgerId = new SSMTextField("lodgerId");
			lodgerId.setLabelKey("page.lbl.ezbiz.searchComtracList.lodgerId");
			add(lodgerId);
			
			SSMTextField invoiceNo = new SSMTextField("invoiceNo");
			invoiceNo.setLabelKey("page.lbl.ezbiz.searchComtracList.invoiceNo");
			add(invoiceNo);

			SSMTextField receiptNo = new SSMTextField("receiptNo");
			receiptNo.setLabelKey("page.lbl.ezbiz.searchComtracList.receiptNo");
			add(receiptNo);
			
			SSMTextField louloaNo = new SSMTextField("louloaNo");
			louloaNo.setLabelKey("page.lbl.ezbiz.searchComtracList.louloaNo");
			add(louloaNo);
			
			SSMDropDownChoice status = new SSMDropDownChoice("status", Parameter.COMTRAC_TRANSACTION_STATUS);
			status.setLabelKey("page.lbl.ezbiz.searchComtracList.status");
			add(status);
			
			SSMDateTextField dtFrom = new SSMDateTextField("dtFrom");
			dtFrom.setLabelKey("page.lbl.ezbiz.searchComtracList.dtFrom");
			add(dtFrom);
			
			SSMDateTextField dtTo = new SSMDateTextField("dtTo");
			dtTo.setLabelKey("page.lbl.ezbiz.comtracList.dtTo");
			add(dtTo);
			
			SSMTextField createdBy = new SSMTextField("createdBy");
			createdBy.setLabelKey("page.lbl.ezbiz.robFormASearch.createBy");
			createdBy.setUpperCase(false);
			add(createdBy);
			
			SSMTextField participantIc = new SSMTextField("participantIc");
			participantIc.setLabelKey("page.lbl.ezbiz.robFormASearch.participantIc");
			add(participantIc);
			
			SSMAjaxButton search = new SSMAjaxButton("search") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					SearchComtracModel searchComtracModel = (SearchComtracModel) form.getDefaultModelObject();
					populateTable(searchComtracModel, target);
				}
			};
			add(search);
			
			populateTable(null, null);
		}
	}
	
	private void populateTable(SearchComtracModel searchComtracModel, AjaxRequestTarget target){
		WebMarkupContainer dataDiv = new WebMarkupContainer("dataDiv");
		dataDiv.setOutputMarkupId(true);
		
		/*ListDataProvider<RobTrainingTransaction> listTransaction = new ListDataProvider<RobTrainingTransaction>(robTrainingTransactionService.searchComtractTransactions(searchComtracModel));*/
		
		List<RobTrainingTransaction> trainingTransaction = new ArrayList<RobTrainingTransaction>();

		if(searchComtracModel != null){
		    trainingTransaction = robTrainingTransactionService.searchComtractTransactions(searchComtracModel);
		}

		ListDataProvider<RobTrainingTransaction> listTransaction = new ListDataProvider<RobTrainingTransaction>(trainingTransaction);
		
		final SSMDataView<RobTrainingTransaction> dataView = new SSMDataView<RobTrainingTransaction>("sorting", listTransaction) {
			private static final long serialVersionUID = 1L;
			
			protected void populateItem(final Item<RobTrainingTransaction> item) {
				final RobTrainingTransaction robTrainingTransaction = item.getModelObject();
				item.add(new SSMLabel("transactionCode", robTrainingTransaction.getTransactionCode()));
				item.add(new SSMLabel("trainingCode", robTrainingTransaction.getTrainingId().getTrainingCode()));
				item.add(new SSMLabel("trainingName", robTrainingTransaction.getTrainingId().getTrainingName()));
				item.add(new SSMLabel("status", robTrainingTransaction.getStatus() ,Parameter.COMTRAC_TRANSACTION_STATUS));
				item.add(new SSMLabel("createBy", robTrainingTransaction.getCreateBy()));
				item.add(new SSMLabel("createDt", robTrainingTransaction.getCreateDt(), "dd/MM/yyyy hh:mm:ss a"));
				
				item.add(new Link("detail", item.getDefaultModel()) {
					public void onClick() {
						RobTrainingTransaction robTrainingTransaction = item.getModelObject();
						if(Parameter.COMTRAC_TRANSACTION_STATUS_data_entry.equals(robTrainingTransaction.getStatus())){
							setResponsePage(new SelectComtracTraining(robTrainingTransaction.getTransactionCode()));
						}else{
							setResponsePage(new ViewListParticipantSummary(robTrainingTransaction.getTransactionCode(), getPage()));
						}
					}
				});

				
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

		dataDiv.add(dataView);
		dataDiv.add(new SSMPagingNavigator("navigator", dataView));
		dataDiv.add(new NavigatorLabel("navigatorLabel", dataView));
		
		if(target == null){
			add(dataDiv);
		}else{
			replace(dataDiv);
			target.add(dataDiv);
		}
		
	}
	
	public class SearchComtracModel implements Serializable{
		private String transactionNo;
		private String lodgerId;
		private String invoiceNo;
		private String receiptNo;
		private String louloaNo;
		private String status;
		private Date dtFrom;
		private Date dtTo;
		private String participantIc;
		private String createdBy;
		
		public String getCreatedBy() {
			return createdBy;
		}
		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}
		public String getTransactionNo() {
			return transactionNo;
		}
		public void setTransactionNo(String transactionNo) {
			this.transactionNo = transactionNo;
		}
		public String getLodgerId() {
			return lodgerId;
		}
		public void setLodgerId(String lodgerId) {
			this.lodgerId = lodgerId;
		}
		public String getInvoiceNo() {
			return invoiceNo;
		}
		public void setInvoiceNo(String invoiceNo) {
			this.invoiceNo = invoiceNo;
		}
		public String getReceiptNo() {
			return receiptNo;
		}
		public void setReceiptNo(String receiptNo) {
			this.receiptNo = receiptNo;
		}
		public String getLouloaNo() {
			return louloaNo;
		}
		public void setLouloaNo(String louloaNo) {
			this.louloaNo = louloaNo;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public Date getDtFrom() {
			return dtFrom;
		}
		public void setDtFrom(Date dtFrom) {
			this.dtFrom = dtFrom;
		}
		public Date getDtTo() {
			return dtTo;
		}
		public void setDtTo(Date dtTo) {
			this.dtTo = dtTo;
		}
		public String getParticipantIc() {
			return participantIc;
		}
		public void setParticipantIc(String participantIc) {
			this.participantIc = participantIc;
		}
	}
	
	@Override
	public String getPageTitle() {
		return "page.title.ezbiz.comtracTransactions";
	}
}
