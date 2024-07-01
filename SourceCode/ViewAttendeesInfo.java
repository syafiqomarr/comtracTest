package com.ssm.ezbiz.comtrac;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.page.BaseFrame;
import com.ssm.llp.base.wicket.component.SSMAjaxLink;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.ezbiz.model.RobTrainingParticipant;

@SuppressWarnings("all")
public class ViewAttendeesInfo extends BaseFrame{
	
	WebMarkupContainer wmcLsNo;
	WebMarkupContainer wmcMmberShipNo;
	WebMarkupContainer wmcFaxNo;
	WebMarkupContainer wmcGender;
	WebMarkupContainer wmcCompany;
	WebMarkupContainer wmcJob_title;
	WebMarkupContainer wmcPostcode;
	WebMarkupContainer wmcCity;
	WebMarkupContainer wmcState;
	WebMarkupContainer wmcDob;
	WebMarkupContainer wmcDesignation;
	WebMarkupContainer wmcDiet;

	
	public ViewAttendeesInfo(final RobTrainingParticipant listParticipant , final ModalWindow viewOwnerPopUp){
		
		wmcLsNo = new WebMarkupContainer("wmcLsNo");
		wmcLsNo.setOutputMarkupId(true);
		wmcLsNo.setVisible(false);
		add(wmcLsNo);
		
		wmcMmberShipNo = new WebMarkupContainer("wmcMmberShipNo");
		wmcMmberShipNo.setOutputMarkupId(true);
		wmcMmberShipNo.setVisible(false);
		add(wmcMmberShipNo);
		
		wmcFaxNo = new WebMarkupContainer("wmcFaxNo");
		wmcFaxNo.setOutputMarkupId(true);
		wmcFaxNo.setVisible(false);
		add(wmcFaxNo);
		
		wmcGender = new WebMarkupContainer("wmcGender");
		wmcGender.setOutputMarkupId(true);
		wmcGender.setVisible(false);
		add(wmcGender);
		
		wmcCompany = new WebMarkupContainer("wmcCompany");
		wmcCompany.setOutputMarkupId(true);
		wmcCompany.setVisible(false);
		add(wmcCompany);
		
		wmcJob_title = new WebMarkupContainer("wmcJob_title");
		wmcJob_title.setOutputMarkupId(true);
		wmcJob_title.setVisible(false);
		add(wmcJob_title);
		
		wmcPostcode = new WebMarkupContainer("wmcPostcode");
		wmcPostcode.setOutputMarkupId(true);
		wmcPostcode.setVisible(false);
		add(wmcPostcode);
		
		wmcCity = new WebMarkupContainer("wmcCity");
		wmcCity.setOutputMarkupId(true);
		wmcCity.setVisible(false);
		add(wmcCity);
		
		wmcState = new WebMarkupContainer("wmcState");
		wmcState.setOutputMarkupId(true);
		wmcState.setVisible(false);
		add(wmcState);
		
		wmcDob = new WebMarkupContainer("wmcDob");
		wmcDob.setOutputMarkupId(true);
		wmcDob.setVisible(false);
		add(wmcDob);
		
		wmcDesignation = new WebMarkupContainer("wmcDesignation");
		wmcDesignation.setOutputMarkupId(true);
		wmcDesignation.setVisible(false);
		add(wmcDesignation);
		
		wmcDiet = new WebMarkupContainer("wmcDiet");
		wmcDiet.setOutputMarkupId(true);
		wmcDiet.setVisible(false);
		add(wmcDiet);
		
		add(new SSMLabel("attendeesName", listParticipant.getName()));
		add(new SSMLabel("attendeesIcNo", listParticipant.getIcNo()));
		add(new SSMLabel("attendeesCompany", listParticipant.getCompany()));
		add(new SSMLabel("attendeesJob_title", listParticipant.getJob_title()));
		add(new SSMLabel("attendeesGender", listParticipant.getGender(), Parameter.GENDER));
		add(new SSMLabel("attendeesFeeType", listParticipant.getFeeType(),Parameter.COMTRAC_FEE_TYPE));
		add(new SSMLabel("attendeesAmount", listParticipant.getAmount()));
		add(new SSMLabel("attendeesPostcode", listParticipant.getPostcode()));
		add(new SSMLabel("attendeesCity", listParticipant.getCity()));
		add(new SSMLabel("attendeesState", listParticipant.getState(), Parameter.CBS_ROB_STATE));
		add(new SSMLabel("attendeesDob", listParticipant.getDob()));
		add(new SSMLabel("attendeesDesignation", listParticipant.getOccupation_code(), Parameter.COMTRAC_DESIGNATION));
		add(new SSMLabel("attendeesDiet", listParticipant.getDiet(), Parameter.DIETARY_TYPE));

		
		if(listParticipant.getLsNo() != null){
			wmcLsNo.setVisible(true);
			wmcLsNo.add(new SSMLabel("attendeesLsNo", listParticipant.getLsNo()));
		}
		
		if(listParticipant.getMembershipNo() != null){
			wmcMmberShipNo.setVisible(true);
			wmcMmberShipNo.add(new SSMLabel("attendeesMmbrShipNo", listParticipant.getMembershipNo()));
		}
		
		add(new SSMLabel("attendeesTelNo", listParticipant.getTelNo()));
		
		if(listParticipant.getFaxNo() != null){
			wmcFaxNo.setVisible(true);
			wmcFaxNo.add(new SSMLabel("attendeesFaxNo", listParticipant.getFaxNo()));
		}
		
		add(new SSMLabel("attendeesEmail", listParticipant.getEmail()));
		
		
		String address = listParticipant.getAddress1();
		if(StringUtils.isNotBlank(listParticipant.getAddress2())){
			address += "\n"+listParticipant.getAddress2();
		}
		if(StringUtils.isNotBlank(listParticipant.getAddress3())){
			address += "\n"+listParticipant.getAddress3();
		}
		
		address = address;
		add(new MultiLineLabel("attendeesAddress", address));
		
		SSMAjaxLink close2 = new SSMAjaxLink("close") {
			@Override
			public void onClick(AjaxRequestTarget arg0) {
				viewOwnerPopUp.close(arg0);
			}
		};
		add(close2);
	}
	
	@Override
	public String getPageTitle() {
		return null;
	}

}
