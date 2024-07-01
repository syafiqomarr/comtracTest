package com.ssm.ezbiz.comtrac;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import com.ssm.ezbiz.otcPayment.SearchBankinSlipDetail;
import com.ssm.ezbiz.otcPayment.ListCollectionCounter.ListCounterFormModel;
import com.ssm.ezbiz.service.RobCounterBankinSlipService;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingFinalListingRemarkService;
import com.ssm.ezbiz.service.RobTrainingFinalListingService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.db.SearchCriteria;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.odt.LLPOdtUtils;
import com.ssm.llp.base.page.SSMPagingNavigator;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSortableDataProvider;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMDateTextField;
import com.ssm.llp.base.wicket.component.SSMDropDownChoice;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMLink;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobCounterBankinSlip;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingFinalListing;
import com.ssm.llp.ezbiz.model.RobTrainingFinalListingRemark;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;

@SuppressWarnings({ "all" })
public class FinalListingPage extends SecBasePage {

	@SpringBean(name = "RobTrainingConfigService")
	RobTrainingConfigService robTrainingConfigService;

	@SpringBean(name = "RobTrainingFinalListingRemarkService")
	RobTrainingFinalListingRemarkService robTrainingFinalListingRemarkService;

	public FinalListingPage() {
		this(null, null);
	}

	public FinalListingPage(String alert, AjaxRequestTarget target) {

		setOutputMarkupId(true);
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
			protected Object load() {
				SearchTrainingModel searchTrainingModel = new SearchTrainingModel();
				return searchTrainingModel;
			}
		}));
		add(new FinalListingPageForm("form", (IModel<SearchTrainingModel>) getDefaultModel()));
	}

	SimpleDateFormat fom = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat pars = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public class FinalListingPageForm extends Form implements Serializable {
		public FinalListingPageForm(String id, IModel<SearchTrainingModel> m) {
			super(id, m);

			SSMTextField finalListingRefNoInput = new SSMTextField("finalListingRefNoInput");
			finalListingRefNoInput.setLabelKey("page.lbl.comtrac.listTraining.finalListingRefNo");
			add(finalListingRefNoInput);

			SSMDateTextField dtFrom = new SSMDateTextField("dtFrom");
			dtFrom.setLabelKey("page.lbl.ecomtrac.dtFrom");
			add(dtFrom);

			SSMDateTextField dtTo = new SSMDateTextField("dtTo");
			dtTo.setLabelKey("page.lbl.ecomtrac.dtTo");
			add(dtTo);

			SSMLabel statusLabel = new SSMLabel("statusLabel",
					new StringResourceModel("page.lbl.ecomtrac.finalListingStatus", this, null));
			add(statusLabel);

			SSMDropDownChoice status = new SSMDropDownChoice("status", Parameter.COMTRAC_FINAL_LISTING_STATUS);
			status.getChoices().add(0, Parameter.PAYMENT_STATUS_ALL);
			add(status);

			SSMAjaxButton search = new SSMAjaxButton("search") {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					SearchTrainingModel searchTrainingModel = (SearchTrainingModel) form.getDefaultModelObject();
					SearchCriteria sc = generateScTemplate(searchTrainingModel.getFinalListingRefNoInput(),
							searchTrainingModel.getDtFrom(), searchTrainingModel.getDtTo(),
							searchTrainingModel.getStatus());
					populateTable(sc, target);
				}
			};
			add(search);

			String userFilterFinance = getCodeTypeWithValue(Parameter.LLP_CONFIG,
					Parameter.LLP_CONFIG_FILTER_USER_LIST_FINANCE);
			String userFilterHosComtrac = getCodeTypeWithValue(Parameter.LLP_CONFIG,
					Parameter.LLP_CONFIG_FILTER_USER_LIST_HOS_COMTRAC);

			SearchCriteria criteria = null;

			try {

				List<String> userListFinance = Arrays.asList(StringUtils.split(userFilterFinance, ","));
				List<String> userListHosComtrac = Arrays.asList(StringUtils.split(userFilterHosComtrac, ","));

				if (StringUtils.isNotBlank(userFilterFinance) && StringUtils.isNotBlank(userFilterHosComtrac)) {

					if (!userListFinance.contains(UserEnvironmentHelper.getLoginName())
							&& !userListHosComtrac.contains(UserEnvironmentHelper.getLoginName())) {
						criteria = new SearchCriteria("createDt", SearchCriteria.IS_NOT_NULL,
								pars.parse(fom.format(new Date()) + " 00:00:00"));
					} else if (userListFinance.contains(UserEnvironmentHelper.getLoginName())) {

						String[] statusForFinance = { Parameter.COMTRAC_FINAL_LISTING_STATUS_approved,
								Parameter.COMTRAC_FINAL_LISTING_STATUS_acknowledge };
						criteria = new SearchCriteria("status", SearchCriteria.IN, statusForFinance);
						criteria.setValues(statusForFinance);
						statusLabel.setVisible(false);
						status.setVisible(false);
					} else if (userListHosComtrac.contains(UserEnvironmentHelper.getLoginName())) {

						String[] statusForHosComtrac = { Parameter.COMTRAC_FINAL_LISTING_STATUS_pending,
								Parameter.COMTRAC_FINAL_LISTING_STATUS_query };
						criteria = new SearchCriteria("status", SearchCriteria.IN, statusForHosComtrac);
						criteria.setValues(statusForHosComtrac);
					}
				}

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			populateTable(criteria, null);
		}

	}

	public void populateTable(SearchCriteria sc, AjaxRequestTarget target) {

		WebMarkupContainer trainingDiv = new WebMarkupContainer("trainingDiv");
		trainingDiv.setOutputMarkupId(true);
		trainingDiv.setVisible(true);

		SSMSortableDataProvider dp = new SSMSortableDataProvider("updateDt", SortOrder.DESCENDING, sc,
				RobTrainingFinalListingService.class);
		final SSMDataView<RobTrainingFinalListing> dataView = new SSMDataView<RobTrainingFinalListing>("sorting", dp) {
			private static final long serialVersionUID = 1L;
			final DecimalFormat df = new DecimalFormat("#,###,##0.00");
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			protected void populateItem(final Item<RobTrainingFinalListing> item) {

				final RobTrainingFinalListing robTrainingFinalListing = item.getModelObject();
				RobTrainingConfig robTrainingConfig = robTrainingConfigService
						.findByTrainingCode(robTrainingFinalListing.getTrainingCode());

				String trainingDt = sdf.format(robTrainingConfig.getTrainingStartDt());
				String status = "PENDING";
				if (!robTrainingConfig.getTrainingEndDt().equals(robTrainingConfig.getTrainingStartDt())) {
					trainingDt += " - " + sdf.format(robTrainingConfig.getTrainingEndDt());
				}

//				Date closingDt = new Date();
//				Calendar c = Calendar.getInstance();
//				c.setTime(robTrainingConfig.getRegClosingDt());
//				c.add(Calendar.DATE, 1);
//				closingDt = c.getTime();

				if (robTrainingFinalListing.getStatus().equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_approved)) {
					status = "APPROVED";
				} else if (robTrainingFinalListing.getStatus().equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_query)) {
					status = "QUERY";
				} else if (robTrainingFinalListing.getStatus()
						.equals(Parameter.COMTRAC_FINAL_LISTING_STATUS_acknowledge)) {
					status = "ACKNOWLEDGED";
				}

				Label statusLabel = new Label("status", status);
				if (status.equals("PENDING")) {
					statusLabel.add(new AttributeAppender("class", " blue"));
				} else if (status.equals("APPROVED")) {
					statusLabel.add(new AttributeAppender("class", " green"));
				} else if (status.equals("QUERY")) {
					statusLabel.add(new AttributeAppender("class", " yellow"));
				} else if (status.equals("ACKNOWLEDGED")) {
					statusLabel.add(new AttributeAppender("class", " brown"));
				}

				item.add(statusLabel);
				item.add(new SSMLabel("finalListingRefNo", robTrainingFinalListing.getFinalListingRefNo()));
				item.add(new SSMLabel("trainingCode", robTrainingConfig.getTrainingCode()));
				item.add(new SSMLabel("trainingName", robTrainingConfig.getTrainingName()));
				item.add(new SSMLabel("trainingDt", trainingDt));
				item.add(new SSMLabel("standardFee", df.format(robTrainingConfig.getStandardFee())));
				item.add(new SSMLabel("specialFee", df.format(robTrainingConfig.getSpecialFee())));
				item.add(new SSMLabel("maxPax", robTrainingConfig.getMaxPax()));
				item.add(new SSMLabel("currPax", robTrainingConfig.getCurrentPax()));

				item.add(new Link("finalListing", item.getDefaultModel()) {
					public void onClick() {
//						RobTrainingFinalListingRemark existingFinalListingRemark = robTrainingFinalListingRemarkService
//								.findExistingFinalListingRefNo(robTrainingFinalListing.getFinalListingRefNo());
//
//						if (existingFinalListingRemark == null) {
//							RobTrainingFinalListingRemark finalListingRemark = new RobTrainingFinalListingRemark();
//							finalListingRemark.setFinalListingRefNo(robTrainingFinalListing.getFinalListingRefNo());
//							finalListingRemark.setQueryBy(UserEnvironmentHelper.getLoginName());
//							robTrainingFinalListingRemarkService.insert(finalListingRemark);
//							setResponsePage(new EditFinalListingPage(robTrainingFinalListing));
//
//						} else {
							setResponsePage(new EditFinalListingPage(robTrainingFinalListing));
//						}
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

		trainingDiv.add(dataView);
		trainingDiv.add(new SSMPagingNavigator("navigator", dataView));
		trainingDiv.add(new NavigatorLabel("navigatorLabel", dataView));

		if (target == null) {
			add(trainingDiv);
		} else {
			replace(trainingDiv);
			target.add(trainingDiv);
		}

	}

	public SearchCriteria generateScTemplate(String finalListingRefNo, Date dateFrom, Date dateTo, String status) {

		SearchCriteria sc = null;

		if (finalListingRefNo != null) {
			sc = new SearchCriteria("finalListingRefNo", SearchCriteria.EQUAL, finalListingRefNo);
		}

		if (dateFrom != null) {
			if (sc != null) {
				try {
					sc = sc.andIfNotNull("createDt", SearchCriteria.GREATER_EQUAL,
							pars.parse(fom.format(dateFrom) + " 00:00:00"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				try {
					sc = new SearchCriteria("createDt", SearchCriteria.GREATER_EQUAL,
							pars.parse(fom.format(dateFrom) + " 00:00:00"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		if (dateTo != null) {
			if (sc != null) {
				try {
					sc = sc.andIfNotNull("createDt", SearchCriteria.LESS_EQUAL,
							pars.parse(fom.format(dateTo) + " 23:59:00"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				try {
					sc = new SearchCriteria("createDt", SearchCriteria.LESS_EQUAL,
							pars.parse(fom.format(dateTo) + " 23:59:00"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		if (sc == null) {
			if (status != null) {
				if (status.equals("ALL")) {
					sc = new SearchCriteria("status", SearchCriteria.IS_NOT_NULL);
				} else {
					sc = new SearchCriteria("status", SearchCriteria.EQUAL, status);
				}
			}
		}

		return sc;
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

	public class SearchTrainingModel implements Serializable {
		private String finalListingRefNoInput;
		private Date dtFrom;
		private Date dtTo;
		private String status;

		public String getFinalListingRefNoInput() {
			return finalListingRefNoInput;
		}

		public void setFinalListingRefNoInput(String finalListingRefNoInput) {
			this.finalListingRefNoInput = finalListingRefNoInput;
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

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}

	@Override
	public String getPageTitle() {
		return "menu.myBiz.listComtracTraining";
	}
}
