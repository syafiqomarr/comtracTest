package com.ssm.ezbiz.comtrac;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.ssm.ezbiz.comtrac.NameListAttendees.SearchModel;
import com.ssm.ezbiz.comtrac.NameListAttendees.SearchParticipantForm;
import com.ssm.ezbiz.service.RobTrainingConfigService;
import com.ssm.ezbiz.service.RobTrainingParticipantService;
import com.ssm.ezbiz.service.RobTrainingTransactionService;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.db.SearchCriteria;
import com.ssm.llp.base.common.model.LlpFileData;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.odt.LLPOdtUtils;
import com.ssm.llp.base.page.SSMPagingNavigator;
import com.ssm.llp.base.page.SecBasePage;
import com.ssm.llp.base.page.table.SSMDataView;
import com.ssm.llp.base.page.table.SSMSessionSortableDataProvider;
import com.ssm.llp.base.page.table.SSMSortableDataProvider;
import com.ssm.llp.base.wicket.SSMDownloadLink;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMDateTextField;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.ezbiz.model.RobTrainingConfig;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;
import com.ssm.llp.ezbiz.model.RobTrainingTransaction;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.mod1.service.LlpUserProfileService;

@SuppressWarnings({ "all" })
public class ReplaceCertificate extends SecBasePage {

	@SpringBean(name = "RobTrainingConfigService")
	RobTrainingConfigService robTrainingConfigService;

	@SpringBean(name = "RobTrainingTransactionService")
	RobTrainingTransactionService robTrainingTransactionService;

	@SpringBean(name = "LlpUserProfileService")
	LlpUserProfileService llpUserProfileService;

	@Override
	public String getPageTitle() {
		return "page.lbl.ezbiz.replaceCertificate.comtracLbl";
	}

	private class SearchListReplaceCertModel {
		private String name;
		private String icNo;
		private String trainingName;
		private String trainingCode;

		private RobTrainingTransaction robTrainingTransaction;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getIcNo() {
			return icNo;
		}

		public void setIcNo(String icNo) {
			this.icNo = icNo;
		}

		public String getTrainingName() {
			return trainingName;
		}

		public void setTrainingName(String trainingName) {
			this.trainingName = trainingName;
		}

		public String getTrainingCode() {
			return trainingCode;
		}

		public void setTrainingCode(String trainingCode) {
			this.trainingCode = trainingCode;
		}

		public RobTrainingTransaction getRobTrainingTransaction() {
			return robTrainingTransaction;
		}

		public void setRobTrainingTransaction(RobTrainingTransaction robTrainingTransaction) {
			this.robTrainingTransaction = robTrainingTransaction;
		}
	}
}
