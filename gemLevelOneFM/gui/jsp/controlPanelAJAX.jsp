<%@ page language="java" contentType="text/html"%>
<%@ page import="java.util.*"%>
<%@ page import="rcms.gui.servlet.pilot.FMPilotConstants"%>
<%@ page isELIgnored="false"%>

<%@ page import="rcms.gui.servlet.pilot.FMPilotBean"%>
<%@ page import="rcms.gui.common.FMPilotState"%>

<%@ taglib prefix="rcms.menu" uri="rcms.menu"%>
<%@ taglib prefix="rcms.control" uri="rcms.control"%>
<%@ taglib prefix="rcms.globalParameter" uri="rcms.globalParameter"%>
<%@ taglib prefix="rcms.notification" uri="rcms.notification"%>

<%!public static boolean isDetachable(HttpServletRequest request) {
    return ((FMPilotBean) request.getAttribute(FMPilotConstants.FM_PILOT_BEAN)).getSessionState()
    .isInputAllowed(FMPilotState.DETACH);
}%>
<rcms.control:menuCreator />

<!doctype html>
<html>
    
    <head>
        <meta Http-Equiv="Cache-Control" Content="no-cache">
        <meta Http-Equiv="Pragma" Content="no-cache">
        <meta Http-Equiv="Expires" Content="0">
        <meta http-equiv="Content-type" content="text/html;charset=UTF-8">
        
        <title>GEM Function Manager</title>
        
        <link rel="StyleSheet" href="../css/common.css" type="text/css" />
        <link rel="StyleSheet" href="../css/control.css" type="text/css" />
        
        <!--  Java script -->
        <rcms.control:customResourceRenderer indentation="0" type="js" path="/js/GUI.js" />

        <script type="text/javascript">
	 var guiInst = new GUI();
        </script>

        <rcms.control:customResourceRenderer indentation="0" type="js"path="/js/jquery-2.1.4.min.js" />
        <rcms.control:customResourceRenderer indentation="0" type="js" path="/js/ajaxRequest.js" />

        <rcms.control:customResourceRenderer indentation="0" type="js" path="/js/ajaxRequestFunctions.js" />
        <rcms.control:customResourceRenderer indentation="0" type="js" path="/js/notifications.js" />
        <rcms.control:customResourceRenderer indentation="0" type="js" path="/js/buttons.js" />

        <rcms.globalParameter:getParameterMap fmParameterContainer="pars" />

        <script type="text/javascript" src="../js/stateNotification.js"></script>
        <script type="text/javascript" src="../js/common.js"></script>
        <script type="text/javascript" src="../js/globalParameters.js"></script>


        <!-- Custom javascript section begin -->
        <script type="text/javascript">
	 <rcms.control:onLoadJSRenderer 
	 reloadOnStateChange="false" 
	 commandButtonCssClass="MyControlButton" 
	 commandParameterCheckBoxTitle="&nbsp;Show Command Parameter Section"
	 commandParameterCssClass="label_left_black" 
	 indentation="2"/>
	 
	 <rcms.control:buttonsJSRenderer indentation="2"/>

	 <rcms.notification:jSRenderer indentation="2"/>

	 <rcms.globalParameter:jSRenderer indentation="2"/>

	 function drawMyCommandButtons(currentState) {
	     // do nothing
	     // placeholder for custom function
	 }
        </script>
        <!-- Custom javascript section end -->

    </head>
    <body class="body">

	<!-- This is the main FMPilotForm, which is required for non-AJAX requests and can be submitted using AJAXRequest. -->
	<form name="FMPilotForm" id="FMPilotForm" method="POST"
action="../../gui/servlet/FMPilotServlet?PAGE=/gui/jsp/controlPanel.jsp">

	    <rcms.control:actionHiddenInputRenderer indentation="1" />
	    <rcms.control:commandHiddenInputRenderer indentation="1" />
	    <rcms.notification:hiddenInputRenderer indentation="1" />
	    <rcms.control:configurationKeyRenderer titleClass="control_label1"
hidden="true" label="Configuration Keys:&nbsp;"
contentClass="control_label2" indentation="1" />

	    <!-- The following part is required for submitting the form using the AJAX Request Library or without AJAX. -->
	    <input type="hidden" id="globalParameterName1"
name="globalParameterName1" value="" />
	    <input type="hidden"
id="globalParameterValue1" name="globalParameterValue1" value="" />
	    <input type="hidden" id="globalParameterType1"
name="globalParameterType1" value="" />
	    <input type="hidden"
id="NO_RESPONSE" name="NO_RESPONSE" value="" />
	</form>

	<%
	// this means that the FMPilot is attached
	if (!isDetachable(request)) {
	%>

	<rcms.control:configurationPathRenderer titleClass="control_label1"
label="Configuration : &nbsp;" contentClass="control_label2"
indentation="10" />

	<%
	}
	%>

	<!-- FMPilot action buttons -->
	<rcms.control:refreshButtonRenderer cssClass="button1"
onClickFunction="onUpdatedRefreshButton()" name="Refresh"
indentation="10" />
	<rcms.control:createButtonRenderer cssClass="button1"
onClickFunction="onCreateButton()" name="Create" indentation="10" />
	<rcms.control:attachButtonRenderer cssClass="button1"
onClickFunction="onAttachButton()" name="Attach" indentation="10" />
	<rcms.control:detachButtonRenderer cssClass="button1"
onClickFunction="onDetachButton()" name="Detach" indentation="10" />
	<rcms.control:destroyButtonRenderer cssClass="button1"
onClickFunction="onDestroyButton()" name="Destroy" indentation="10" />

	<%
	if (!isDetachable(request)) {
	    out.println("</body></html>");
	    return; // don't show full page if not attached
	}
	%>
	
	<p>
	    The following text box displays a simple StringT FM parameter that can not be modified directly and gets updated
	    using the notification system.
	    <br />
	    <br /> stringParameter1:
	    <input readonly="readonly" type="text" id="stringParameter1" size="50"
value="${pars.STRING_PARAMETER1}" />
	</p>
	<br />

	<!-- You should definitely check the values your are updating for valid types
	     (only send an actual integer for an IntegerT parameter etc.).
	     For example with JavaScript's parseInt, isNaN, isNumeric etc. for integers and string comparison for booleans.
	     Also check for valid value ranges (for example numbers that are too big to fit into a Java integer). -->

	<p>
	    The following text box displays a simple IntegerT FM parameter that can not be modified directly.<br />
	    The function manager increases this parameter by one every 500 milliseconds and the page is updated using notifications.<br />
	    Clicking the button sets the FM parameter to zero using AJAX.
	    <br />
	    <br />integerParameter:
	    <input readonly="readonly" type="text" id="integerParameter"
value="${pars.INTEGER_PARAMETER}" />
	    <button
onclick="guiInst.setFMParameter('INTEGER_PARAMETER', 0, guiInst.PARAMETER_TYPE_INTEGER); return false;">reset
		counter</button>
	    (using jQuery)
	</p>
	<br />	
	<p>
	    The following text box displays a simple BooleanT FM parameter that can be toggled using the button next to it.<br />
	    The new boolean is determined on the client and then sent as a simple parameter update. With many concurrent users it might be
	    a better idea to use a GUI command instead and let the FM toggle the parameter.
	    <br />
	    <br />booleanParameter:
	    <input readonly="readonly" type="text" id="booleanParameter"
value="${pars.BOOLEAN_PARAMETER}" />
	    <button
onclick="ajaxRequestSetFMParameter('BOOLEAN_PARAMETER', !($('#booleanParameter').val() == 'true'), guiInst.PARAMETER_TYPE_BOOLEAN); return false;">toggle</button>
	    (using AJAXLibrary)
	</p>
	
	<script type="text/javascript">
	 guiInst.attach(document);

	 // a call to onLoad is needed since it starts the notification system
	 $(document).ready(function() {
	     onLoad();
	 });
	</script>

    </body>
</html>
