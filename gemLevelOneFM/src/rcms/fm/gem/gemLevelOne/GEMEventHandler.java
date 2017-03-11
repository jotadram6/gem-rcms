package rcms.fm.gem.gemLevelOne;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.lang.Integer;
import java.lang.Double;

import java.math.BigInteger;

import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import java.util.regex.MatchResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.DOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.soap.SOAPException;

import rcms.common.db.DBConnectorException;

import rcms.errorFormat.CMS.CMSError;

import rcms.fm.fw.EventHandlerException;
import rcms.fm.fw.StateEnteredEvent;

import rcms.fm.fw.parameter.CommandParameter;
import rcms.fm.fw.parameter.FunctionManagerParameter;
import rcms.fm.fw.parameter.Parameter;
import rcms.fm.fw.parameter.ParameterException;
import rcms.fm.fw.parameter.ParameterSet;
import rcms.fm.fw.parameter.type.BooleanT;
import rcms.fm.fw.parameter.type.DateT;
import rcms.fm.fw.parameter.type.IntegerT;
import rcms.fm.fw.parameter.type.LongT;
import rcms.fm.fw.parameter.type.ParameterType;
import rcms.fm.fw.parameter.type.StringT;
import rcms.fm.fw.parameter.type.VectorT;
import rcms.fm.fw.service.parameter.ParameterServiceException;

import rcms.fm.fw.user.UserActionException;
import rcms.fm.fw.user.UserStateNotificationHandler;

import rcms.fm.resource.CommandException;
import rcms.fm.resource.QualifiedGroup;
import rcms.fm.resource.QualifiedResource;
import rcms.fm.resource.QualifiedResourceContainer;
import rcms.fm.resource.QualifiedResourceContainerException;
import rcms.fm.resource.qualifiedresource.FunctionManager;
import rcms.fm.resource.qualifiedresource.JobControl;
import rcms.fm.resource.qualifiedresource.XdaqApplication;
import rcms.fm.resource.qualifiedresource.XdaqApplicationContainer;
import rcms.fm.resource.qualifiedresource.XdaqExecutive;

import rcms.resourceservice.db.Group;
import rcms.resourceservice.db.resource.Resource;

import rcms.stateFormat.StateNotification;
import rcms.statemachine.definition.Input;
import rcms.util.logger.RCMSLogger;
import rcms.xdaqctl.XDAQParameter;
import rcms.xdaqctl.XDAQMessage;

import rcms.util.logsession.LogSessionConnector;
import rcms.util.logsession.LogSessionException;

// needed to store RunInfo
import rcms.utilities.runinfo.RunInfo;
import rcms.utilities.runinfo.RunInfoConnectorIF;
import rcms.utilities.runinfo.RunInfoException;
import rcms.utilities.runinfo.RunNumberData;
import rcms.utilities.runinfo.RunSequenceNumber;

//
import net.hep.cms.xdaqctl.WSESubscription;
import net.hep.cms.xdaqctl.XDAQException;
import net.hep.cms.xdaqctl.XDAQMessageException;
import net.hep.cms.xdaqctl.XDAQTimeoutException;
import net.hep.cms.xdaqctl.XMASMessage;
import net.hep.cms.xdaqctl.xdata.FlashList;
import net.hep.cms.xdaqctl.xdata.SimpleItem;
import net.hep.cms.xdaqctl.xdata.XDataType;

import rcms.util.logsession.LogSessionConnector;
import rcms.util.logsession.LogSessionException;

import net.hep.cms.xdaqctl.WSESubscription;
import net.hep.cms.xdaqctl.XDAQMessageException;
import net.hep.cms.xdaqctl.XDAQTimeoutException;
import net.hep.cms.xdaqctl.XMASMessage;
import net.hep.cms.xdaqctl.xdata.FlashList;
import net.hep.cms.xdaqctl.xdata.SimpleItem;
import net.hep.cms.xdaqctl.xdata.XDataType;

import rcms.fm.gem.gemLevelOne.parameters.GEMParameters;
import rcms.fm.gem.gemLevelOne.util.GEMUtil;

/**
 *
 * Main Event Handler class for GEM Level 1 Function Manager.
 *
 * @author Andrea Petrucci, Alexander Oh, Michele Gulmini
 * @maintainer Jared Sturdy
 */
public class GEMEventHandler extends UserStateNotificationHandler {

    /**
     * <code>RCMSLogger</code>: RCMS log4j logger.
     */
    static RCMSLogger logger = new RCMSLogger(GEMEventHandler.class);

    GEMFunctionManager functionManager = null;

    private QualifiedGroup qualifiedGroup  = null;
    public static final String XDAQ_NS = "urn:xdaq-soap:3.0";
    private RunNumberData _myRunNumberData = null;

    String FullTCDSControlSequence  =  "not set";  // Config doc for iCI
    String FullLPMControlSequence   =  "not set";  // Config doc for LPM
    String FullPIControlSequence    =  "not set";  // Config doc for PI

    // parameters for tts test (unnecessary for GEM?)
    private XDAQParameter   svTTSParameter  = null;
    private final ScheduledExecutorService scheduler;

    protected RunSequenceNumber runNumberGenerator = null;
    protected RunNumberData     runNumberData      = null;
    protected Integer           runNumber          = null;
    protected boolean           localRunNumber     = false;
    protected RunInfo           runInfo            = null;

    // Connector to log session db, used to create session identifiers
    public LogSessionConnector logSessionConnector;

    /**
     * Handle to utility class
     */
    private GEMUtil GEMUtil;

    public GEMEventHandler() throws rcms.fm.fw.EventHandlerException {
	// this handler inherits UserStateNotificationHandler
	// so it is already registered for StateNotification events

	// Let's register also the StateEnteredEvent triggered when the FSM enters in a new state.
	subscribeForEvents(StateEnteredEvent.class);

	addAction(GEMStates.INITIALIZING,	    "initAction"         );
	addAction(GEMStates.HALTING,     	    "haltAction"         );
	addAction(GEMStates.CONFIGURING, 	    "configureAction"    );
	// addAction(GEMStates.ENABLING, 	    "enableAction"          );
	addAction(GEMStates.STARTING,    	    "startAction"        );
	addAction(GEMStates.RESUMING,    	    "resumeAction"       );
	addAction(GEMStates.STOPPING,    	    "stopAction"         );
	addAction(GEMStates.PAUSING,     	    "pauseAction"        );
	addAction(GEMStates.RECOVERING,  	    "recoverAction"      );
	addAction(GEMStates.RESETTING,   	    "resetAction"        );
        addAction(GEMStates.COLDRESETTING,          "coldResettingAction");

        addAction(GEMStates.RUNNING,                  "runningAction"                 );  // for testing with external inputs
        addAction(GEMStates.RUNNINGDEGRADED,          "runningDegradedAction"         );  // for testing with external inputs
        addAction(GEMStates.RUNNINGSOFTERRORDETECTED, "runningSoftErrorDetectedAction");  // for testing with external inputs
        addAction(GEMStates.FIXINGSOFTERROR,          "fixSoftErrorAction"            );

	addAction(GEMStates.PREPARING_TTSTEST_MODE, "preparingTTSTestModeAction");
	addAction(GEMStates.TESTING_TTS,    	    "testingTTSAction"          );

        scheduler = Executors.newScheduledThreadPool(1);
    }


    @Override
    public void init() throws rcms.fm.fw.EventHandlerException {
	functionManager = (GEMFunctionManager) getUserFunctionManager();
	qualifiedGroup  = functionManager.getQualifiedGroup();
	// Unique id for each configuration

	int session_id = functionManager.getQualifiedGroup().getGroup().getDirectory().getId();
	functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.SID, new IntegerT(session_id)));

	// instantiate utility
	GEMUtil = new GEMUtil(functionManager);

	// debug
	logger.debug("[GEMEventHandler init] GEM levelOneFM init() called: functionManager=" + functionManager );
	// call renderers
	//GEMUtil.renderMainGui();
    }

	public void destroy() {
		functionManager = (GEMFunctionManager) getUserFunctionManager();
		qualifiedGroup  = functionManager.getQualifiedGroup();

		/*FROM CTPPS
		if(timer!=null) {
			timer.cancel(); 
			timer = null;
		}
		*/
		
		// we destroy -> Halt TCDS ICIs/PIs
		ListIterator<QualifiedResource> itr = functionManager.getXdaqTCDSAppIterator();
		while(itr.hasNext()){
			XdaqApplication xdaqApp = (XdaqApplication) itr.next();		
			Input input = new Input("Halt");
			try {
				xdaqApp.execute(input, new String(functionManager.getName()), new String(functionManager.getURI().toURL().toString()));
			} catch (CommandException e) {
				String errMsg = "Unable to send input to: " + xdaqApp.getRole() + ". Stacktrace: " + e.getMessage();
				logger.error(errMsg);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
		}
		
		// debug
		logger.debug("destroy() called: functionManager=" + functionManager );
		//super.destroy();
	}
    
    public void initAction(Object obj) throws UserActionException {

    	if (obj instanceof StateNotification) {
    		
    		// triggered by State Notification from child resource

    		/************************************************
    		 * PUT YOUR CODE HERE
    		 ***********************************************/

    		return;	
    	}

    	else if (obj instanceof StateEnteredEvent) {

    		// triggered by entered state action
    		// let's command the child resources
    		//GEMUtil.setParameter(action ,"Initializing" );

    		// debug
    		logger.debug("[GEMEventHandler initAction] initAction called.");

    		// set action
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("Initializing")));

    		// update state
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.STATE,
                                                                                        new StringT(functionManager.getState().getStateString())));

    		// initialize qualified group
    		try {
    			qualifiedGroup.init();
    		} catch (Exception e) {
    			// failed to init
    			String errMsg = this.getClass().toString() + " failed to initialize resources";

    			// send error notification
    			sendCMSError(errMsg);

    			//log error
    			logger.error("[GEMEventHandler initAction] "+errMsg,e);

    			// go to error state
    			functionManager.fireEvent(GEMInputs.SETERROR);
    		}

    		// find xdaq applications
    		List<QualifiedResource> xdaqList = qualifiedGroup.seekQualifiedResourcesOfType(new XdaqApplication());
    		functionManager.containerXdaqApplication = new XdaqApplicationContainer(xdaqList);
    		logger.debug("[GEMEventHandler initAction] Application list : " + xdaqList.size() );

    		// set paraterts from properties
    		GEMUtil.setParameterFromProperties();

    		// render gui
    		GEMUtil.renderMainGui();

    		functionManager.containerGEMSupervisor =
    				new XdaqApplicationContainer(functionManager.containerXdaqApplication.getApplicationsOfClass("gem::supervisor::GEMSupervisor"));

    		// go to HALT
    		functionManager.fireEvent( GEMInputs.SETHALTED );

    		// set action
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,new StringT("")));

    		// parameters for cross checks (RCMS task #12155)
    		ParameterSet  parameters = getUserFunctionManager().getLastInput().getParameterSet();
    		IntegerT             sid = (IntegerT)parameters.get(GEMParameters.SID).getValue();
    		StringT  global_conf_key = (StringT)parameters.get(GEMParameters.GLOBAL_CONF_KEY).getValue();
    		functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.SID, sid));
    		functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.INITIALIZED_WITH_SID,
                                                                                         sid));
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.INITIALIZED_WITH_GLOBAL_CONF_KEY,
                                                                                        global_conf_key ));

    		logger.info("[GEMEventHandler initAction] initAction Executed");
    	}
    }


    public void resetAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    return;
	}

	else if (obj instanceof StateEnteredEvent) {


	    // triggered by entered state action
	    // let's command the child resources

	    // debug
	    logger.debug("[GEMEventHandler resetAction] resetAction called.");

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("Resetting")));
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.STATE,
                                                                                        new StringT(functionManager.getState().getStateString())));

	    // destroy xdaq
	    destroyXDAQ();

	    // reset qualified group
	    QualifiedGroup qualifiedGroup = functionManager.getQualifiedGroup();
	    qualifiedGroup.reset();

	    // reinitialize qualified group
	    try {
		qualifiedGroup.init();
	    }
	    catch (Exception e) {
		String errorMess = e.getMessage();
		logger.error("[GEMEventHandler resetAction] "+errorMess);
		throw new UserActionException(errorMess);
	    }

	    // go to Initital
	    functionManager.fireEvent( GEMInputs.SETHALTED );

	    // Clean-up of the Function Manager parameters
	    cleanUpFMParameters();

	    logger.info("[GEMEventHandler resetAction] resetAction Executed");
	}
    }

    public void recoverAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    return;
	}

	else if (obj instanceof StateEnteredEvent) {

	    System.out.println("Executing recoverAction");
	    logger.info("[GEMEventHandler recoverAction] Executing recoverAction");

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,new StringT("recovering")));
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(
											GEMParameters.STATE,new StringT(functionManager.getState().getStateString())));

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    // leave intermediate state
	    functionManager.fireEvent( GEMInputs.SETINITIAL );

	    // Clean-up of the Function Manager parameters
	    cleanUpFMParameters();

	    logger.info("[GEMEventHandler recoverAction] recoverAction Executed");
	}
    }

    public void configureAction(Object obj) throws UserActionException {

    	if (obj instanceof StateNotification) {

    		// triggered by State Notification from child resource

    		// leave intermediate state
    		// check that the gem supervisor is in configured state
    		for ( XdaqApplication xdaqApp : functionManager.containerGEMSupervisor.getApplications()) {
    			if (xdaqApp.getCacheState().equals(GEMStates.ERROR)) {
    				functionManager.fireEvent(GEMInputs.SETERROR);
    			}
    			else if (!xdaqApp.getCacheState().equals(GEMStates.CONFIGURED)) return;
    		}
    		functionManager.fireEvent( GEMInputs.SETCONFIGURED );

    		return;
    	}

    	else if (obj instanceof StateEnteredEvent) {
    		System.out.println("Executing configureAction");
    		logger.info("[GEMEventHandler configureAction] Executing configureAction");


    		// check that we have a gem supervisor to control
    		if (functionManager.containerGEMSupervisor.getApplications().size() == 0) {
    			// nothing to control, go to configured immediately
    			functionManager.fireEvent( GEMInputs.SETCONFIGURED );
    			return;
    		}

    		// set action
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,new StringT("configuring")));
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.STATE, new StringT("Configured")));

    		//get the parameters of the command
    		ParameterSet<CommandParameter> parameterSet = getUserFunctionManager().getLastInput().getParameterSet();

    		// check parameter set
    		if (parameterSet.size()==0 || parameterSet.get(GEMParameters.RUN_TYPE) == null ||
    		((StringT)parameterSet.get(GEMParameters.RUN_TYPE).getValue()).equals("") )  {
    			// Set default
    			try {
    				parameterSet.add( new CommandParameter<StringT>(GEMParameters.RUN_TYPE, new StringT("Default")));
    			} catch (ParameterException e) {
    				logger.error("[GEMEventHandler configureAction] Could not default the run type to Default",e);
    				functionManager.fireEvent(GEMInputs.SETERROR);
    			}
    		}

    		// get the run number from the configure command
    		// get the calib key and put it as run type (another ichiro special)
    		String runType = ((StringT)functionManager.getParameterSet().get(GEMParameters.GEM_CALIB_KEY).getValue()).getString();

    		// Set the runType in the Function Manager parameters
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.RUN_TYPE,new StringT(runType)));


    		// set run type parameter
    		try {
    			XDAQParameter xdaqParam = ((XdaqApplication)
					   functionManager.containerGEMSupervisor.getApplications().get(0))
					   .getXDAQParameter();

    			// select RunType of gem supervisor.
    			// this parameter is used to differentiate between
    			// 1) calibration run
    			// 2) global run
    			// since level one is only used in global runs we hardwire
    			// for the time being to global run = "Default".
    			// the parameter is set to the supervisor xdaq application.
    			xdaqParam.select("RunType");
    			xdaqParam.setValue("RunType", runType);
    			xdaqParam.send();

    		} catch (Exception e) {
    			logger.error("[GEMEventHandler configureAction] "+getClass().toString() +
			     "Failed to set run type Default to gem supervisor xdaq application."+
                             " (Value is hardwired in the code) ", e);

    			functionManager.fireEvent(GEMInputs.SETERROR);
    		}

    		// send Configure
    		try {
    			functionManager.containerGEMSupervisor.execute(GEMInputs.CONFIGURE);

    		} catch (Exception e) {
    			logger.error("[GEMEventHandler configureAction] "+getClass().toString() +
			     "Failed to Configure gem supervisor xdaq application.", e);

    			functionManager.fireEvent(GEMInputs.SETERROR);
    		}

    		// set action
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,new StringT("")));

    		// parameters for cross checks (RCMS task #12155)
    		ParameterSet parameters = getUserFunctionManager().getLastInput().getParameterSet();
    		IntegerT run_number      = (IntegerT)parameters.get(GEMParameters.RUN_NUMBER     ).getValue();
    		StringT  run_key         = (StringT) parameters.get(GEMParameters.RUN_KEY        ).getValue();
    		StringT  tpg_key         = (StringT) parameters.get(GEMParameters.TPG_KEY        ).getValue();
    		StringT  fed_enable_mask = (StringT) parameters.get(GEMParameters.FED_ENABLE_MASK).getValue();
    		functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.CONFIGURED_WITH_RUN_NUMBER     , run_number));
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT> (GEMParameters.CONFIGURED_WITH_RUN_KEY        , run_key));
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT> (GEMParameters.CONFIGURED_WITH_TPG_KEY        , tpg_key));
    		functionManager.getParameterSet().put(new FunctionManagerParameter<StringT> (GEMParameters.CONFIGURED_WITH_FED_ENABLE_MASK, fed_enable_mask));

			// Configure TCDS ICIs/PIs 
			ListIterator<QualifiedResource> itr = functionManager.getXdaqTCDSAppIterator();
			while(itr.hasNext()){
				XdaqApplication xdaqApp = (XdaqApplication) itr.next();
			
				String appName = xdaqApp.getName(); //returns <classname>_<instance>
				String confPar = appName + "_" + "conf";
				String hwConf = functionManager.getConfProperty(confPar);
				logger.debug(hwConf);
				
				//Define the FSM input to be send
				Input inputCfg = new Input("Configure");
				ParameterSet<CommandParameter> pSetCfg = new ParameterSet<CommandParameter>();
				
				if( hwConf != null ){
					try {
						pSetCfg.add(new CommandParameter<StringT> ("hardwareConfigurationString", new StringT(hwConf)));
						// Add FEDEnableMask to the IPM TCDS configuration parameters application
						if(appName.contains("PI") )
								pSetCfg.add(new CommandParameter<StringT>("fedEnableMask", new StringT(fedEnableMask)));
					} catch (ParameterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					inputCfg.setParameters(pSetCfg);
					//send the message to XDAQ 		
					try {
						xdaqApp.execute(inputCfg, new String(functionManager.getName()), rcmsURL);
					} catch (CommandException e) {
						String errMsg = "Unable to send input to: " + xdaqApp.getRole() + ". Stacktrace: " + e.getMessage();
						logger.error(errMsg);
					}
				}
				else {
					String errMsg = "No configuration found with name: " + confPar + ", in Resource Configurator for resource: " + xdaqApp.getRole(); 
					logger.error(errMsg);
				}
				//timer.scheduleAtFixedRate(new LeaseTask(xdaqApp, new String(functionManager.getName())), LEASE_INIT_WAIT, LEASE_INTERVAL);
			}	
			// Configure TCDS ICIs and PIs

	    logger.info("[GEMEventHandler configureAction] configureAction Executed");
	}
    }

    public void startAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    // leave intermediate state
	    // check that the gem supervisor is in configured state
	    for ( XdaqApplication xdaqApp : functionManager.containerGEMSupervisor.getApplications()) {
		if (xdaqApp.getCacheState().equals(GEMStates.ERROR)) {
		    functionManager.fireEvent(GEMInputs.SETERROR);
		}
		else if (xdaqApp.getCacheState().equals(GEMStates.ERROR)) {
		    functionManager.fireEvent(GEMInputs.SETERROR);
		}
		else if (!xdaqApp.getCacheState().equals(GEMStates.RUNNING)) return;
	    }
	    functionManager.fireEvent( GEMInputs.SETRUNNING );

	    return;
	}

	else if (obj instanceof StateEnteredEvent) {
	    System.out.println("Executing startAction");
	    logger.info("[GEMEventHandler startAction] Executing startAction");

	    // check that we have a gem supervisor to control
	    if (functionManager.containerGEMSupervisor.getApplications().size() == 0) {
		// nothing to control, go to configured immediately
		functionManager.fireEvent( GEMInputs.SETRUNNING );
		return;
	    }

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("starting")));
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.STATE,
                                                                                        new StringT("Running")));

	    // get the parameters of the command
	    ParameterSet<CommandParameter> parameterSet = getUserFunctionManager().getLastInput().getParameterSet();
	    runNumber = null;
	    localRunNumber = false;


	    // check parameter set
	    if (parameterSet.size()==0 || parameterSet.get(GEMParameters.RUN_NUMBER) == null )  {
		// go to error, we require parameters
		String errMsg = "startAction: no parameters given with start command";

		// log remark
		logger.info("[GEMEventHandler startAction] "+errMsg);

		RunInfoConnectorIF ric = functionManager.getRunInfoConnector();
		if ( ric != null ) {
                    if ( runNumberGenerator == null ) {
                        logger.info("[GEMEventHandler startAction] Creating RunNumberGenerator (RunSequenceNumber) ");
                        runNumberGenerator = new RunSequenceNumber(ric,functionManager.getOwner(),
                                                                   functionManager.getParameterSet().
                                                                   get(GEMParameters.SEQ_NAME).getValue().toString());
                    }

                    if ( runNumberGenerator != null ) {
                        runNumberData = runNumberGenerator.createRunSequenceNumber(new Integer(functionManager.getParameterSet().
                                                                                               get(GEMParameters.SID).
                                                                                               getValue().toString()));
                    }

                    if ( runNumberData != null ) {
                        runNumber =  runNumberData.getRunNumber();
                        System.out.println("Generated RunNumber\n"+runNumberData.toString());
                        localRunNumber = true;
                    } else {
                        logger.error("[GEMEventHandler startAction] Error generating RunNumber");
                    }
                } else {
                    logger.error("[GEMEventHandler startAction] Failed to create RunNumberGenerator");
                }
	    } else {
                // get the run number from the start command
                runNumber =((IntegerT)parameterSet.get(GEMParameters.RUN_NUMBER).getValue()).getInteger();
            }

	    if ( runNumber != null ) {
                // Set the run number in the Function Manager parameters
                functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.RUN_NUMBER,
                                                                                             new IntegerT(runNumber)));
            } else {
                logger.error("[GEMEventHandler startAction] Cannot find and/or generator RunNumber");
            }

	    publishRunInfo(true);


	    /*
	    // default to -1
	    try {
	    parameterSet.add( new CommandParameter<IntegerT>(GEMParameters.RUN_NUMBER, new IntegerT(-1)));
	    } catch (ParameterException e) {
	    logger.error("[GEMEventHandler startAction] Could not default the run number to -1",e);
	    functionManager.fireEvent(GEMInputs.SETERROR);
	    }

	    // log this
	    logger.warn("[GEMEventHandler startAction] No run number given, defaulting to -1.");
	    */



	    // get the run number from the start command
	    //Integer runNumber = ((IntegerT)parameterSet.get(GEMParameters.RUN_NUMBER).getValue()).getInteger();

	    // Set the run number in the Function Manager parameters
	    //functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.RUN_NUMBER,new IntegerT(-1)));

	    //			set run number parameter
	    try {
		XDAQParameter xdaqParam = ((XdaqApplication)
					   functionManager.containerGEMSupervisor.getApplications().get(0)).getXDAQParameter();

		xdaqParam.select("RunNumber");
		ParameterSet<CommandParameter> commandParam = getUserFunctionManager().getLastInput().getParameterSet();
		if (commandParam == null) {
		    logger.error("[GEMEventHandler startAction] "+getClass().toString()+
                                 "Failded to Enable XDAQ, no run # specified.");
		    functionManager.fireEvent(GEMInputs.SETERROR);
		}
		logger.debug("[GEMEventHandler startAction] "+getClass().toString()+
                             "Run #: " + runNumber);

		xdaqParam.setValue("RunNumber", runNumber.toString());
		xdaqParam.send();

	    } catch (Exception e) {
		logger.error("[GEMEventHandler startAction] "+getClass().toString()+
                             "Failed to Enable gem supervisor XDAQ application.", e);

		functionManager.fireEvent(GEMInputs.SETERROR);
	    }

	    // send Enable
	    try {
		functionManager.containerGEMSupervisor.execute(new Input("Start"));

	    } catch (Exception e) {
		logger.error("[GEMEventHandler startAction] "+getClass().toString()+
                             "Failed to Enable gem supervisor XDAQ application.", e);

		functionManager.fireEvent(GEMInputs.SETERROR);
	    }

	    logger.debug("[GEMEventHandler startAction] GEMLeadingActions.start ... done.");

            // set action
            functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,new StringT("")));

	    // parameters for cross checks (RCMS task #12155)
	    ParameterSet parameters = getUserFunctionManager().getLastInput().getParameterSet();
	    IntegerT run_number      = (IntegerT)parameters.get(GEMParameters.RUN_NUMBER).getValue();
	    functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.STARTED_WITH_RUN_NUMBER,
                                                                                         run_number));

            logger.debug("[GEMEventHandler startAction] startAction Executed");
        }
    }

    public void pauseAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    return;
	} else if (obj instanceof StateEnteredEvent) {
	    System.out.println("Executing pauseAction");
	    logger.info("[GEMEventHandler pauseAction] Executing pauseAction");

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("pausing")));

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    // leave intermediate state
	    functionManager.fireEvent( GEMInputs.SETPAUSED );

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("")));

            logger.debug("[GEMEventHandler pauseAction] pauseAction Executed");
        }
    }

    public void stopAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    // leave intermediate state
	    // check that the gem supervisor is in configured state
	    for ( XdaqApplication xdaqApp : functionManager.containerGEMSupervisor.getApplications()) {
		if (xdaqApp.getCacheState().equals(GEMStates.ERROR)) {
		    functionManager.fireEvent(GEMInputs.SETERROR);
		}
		else if (!xdaqApp.getCacheState().equals(GEMStates.CONFIGURED)) return;
	    }
	    functionManager.fireEvent( GEMInputs.SETCONFIGURED );

	    return;
	} else if (obj instanceof StateEnteredEvent) {
	    System.out.println("Executing stopAction");
	    logger.info("[GEMEventHandler stopAction] Executing stopAction");

	    // check that we have a gem supervisor to control
	    if (functionManager.containerGEMSupervisor.getApplications().size() == 0) {
		// nothing to control, go to configured immediately
		functionManager.fireEvent( GEMInputs.SETCONFIGURED );
		return;
	    }

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("stopping")));
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.STATE,
                                                                                        new StringT("Halted")));

	    //Run Info
	    publishRunInfo(false);

	    // send Stop
	    try {
		functionManager.containerGEMSupervisor.execute(new Input("Stop"));

	    } catch (Exception e) {
		logger.error("[GEMEventHandler stopAction] "+getClass().toString()+
                             "Failed to Stop XDAQ.", e);

		functionManager.fireEvent(GEMInputs.SETERROR);
	    }


	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("")));

	    logger.debug("[GEMEventHandler stopAction] stopAction Executed");
	}
    }
    public void resumeAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    return;
	} else if (obj instanceof StateEnteredEvent) {
	    System.out.println("Executing resumeAction");
	    logger.info("[GEMEventHandler resumeAction] Executing resumeAction");

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("resuming")));

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    // leave intermediate state
	    functionManager.fireEvent( GEMInputs.SETRESUMED );

	    // Clean-up of the Function Manager parameters
	    cleanUpFMParameters();

	    logger.debug("[GEMEventHandler resumeAction] resumeAction Executed");
	}
    }

    public void haltAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    //			triggered by State Notification from child resource

	    // leave intermediate state
	    // check that the gem supervisor is in configured state
	    for ( XdaqApplication xdaqApp : functionManager.containerGEMSupervisor.getApplications()) {
		if (xdaqApp.getCacheState().equals(GEMStates.ERROR)) {
		    functionManager.fireEvent(GEMInputs.SETERROR);
		}
		else if (!xdaqApp.getCacheState().equals(GEMStates.HALTED)) return;
	    }
	    functionManager.fireEvent( GEMInputs.SETHALTED );

	    return;
	} else if (obj instanceof StateEnteredEvent) {
	    System.out.println("Executing haltAction");
	    logger.info("[GEMEventHandler haltAction] Executing haltAction");

	    // check that we have a gem supervisor to control
	    if (functionManager.containerGEMSupervisor.getApplications().size() == 0) {
		// nothing to control, go to configured immediately
		functionManager.fireEvent( GEMInputs.SETHALTED );
		return;
	    }

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("halting")));

	    // send Halt
	    try {
		functionManager.containerGEMSupervisor.execute(new Input("Halt"));
	    } catch (Exception e) {
		logger.error("[GEMEventHandler haltAction] "+getClass().toString()+
                             "Failed to Halt XDAQ.", e);

		functionManager.fireEvent(GEMInputs.SETERROR);
	    }


	    // check from which state we came.
	    if (functionManager.getPreviousState().equals(GEMStates.TTSTEST_MODE)) {
		// when we came from TTSTestMode we need to
		// 1. give back control of sTTS to HW
	    }


	    // Clean-up of the Function Manager parameters
	    cleanUpFMParameters();

	    logger.debug("[GEMEventHandler haltAction] haltAction Executed");
	}
    }

    public void preparingTTSTestModeAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    StateNotification sn = (StateNotification)obj;

	    if (sn.getToState().equals(GEMStates.CONFIGURED.getStateString())) {
		// configure is send in state entered part (below)
		// here we receive "Configured" from supervisor.
		// send a enable now.
		try {
		    functionManager.containerGEMSupervisor.execute(new Input("Start"));
		} catch (QualifiedResourceContainerException e) {
		    logger.error("[GEMEventHandler preparingTTSTestModeActionAction] Could not enable gem supervisor.",e);
		    functionManager.fireEvent(GEMInputs.SETERROR);
		}
	    } else if ( sn.getToState().equals(GEMStates.RUNNING.getStateString()) ) {
		// recveived runnning state. now we can
		// leave intermediate state
		functionManager.fireEvent( GEMInputs.SETTTSTEST_MODE );
	    } else {
		// don't understand state, go to error.
		logger.error("[GEMEventHandler preparingTTSTestModeActionAction] Unexpected state notification in state "
			     + functionManager.getState().toString()
			     + ", received state :"
			     + sn.getToState());
		functionManager.fireEvent(GEMInputs.SETERROR);
	    }
	    return;
	} else if (obj instanceof StateEnteredEvent) {
	    System.out.println("Executing preparingTestModeAction");
	    logger.info("[GEMEventHandler preparingTTSTestModeActionAction] Executing preparingTestModeAction");

	    // check that we have a gem supervisor to control
	    if (functionManager.containerGEMSupervisor.getApplications().size() == 0) {
		// nothing to control, go to configured immediately
		functionManager.fireEvent( GEMInputs.SETTTSTEST_MODE );
		return;
	    }

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,new StringT("preparingTestMode")));

	    // set run type parameter
	    try {
		XDAQParameter xdaqParam = ((XdaqApplication)functionManager.containerGEMSupervisor.getApplications().get(0))
                    .getXDAQParameter();

		xdaqParam.select("RunType");
		xdaqParam.setValue("RunType", "sTTS_Test");
		xdaqParam.send();

	    } catch (Exception e) {
		logger.error("[GEMEventHandler preparingTTSTestModeAction] "+getClass().toString()+
			     "Failed to set run type: " + "sTTS_Test", e);

		functionManager.fireEvent(GEMInputs.SETERROR);
	    }

	    try {
		functionManager.containerGEMSupervisor.execute(new Input("Configure"));
	    } catch (Exception e) {
		logger.error("[GEMEventHandler preparingTTSTestModeActionAction] "+getClass().toString()+
			     "Failed to TTSPrepare XDAQ.", e);

		functionManager.fireEvent(GEMInputs.SETERROR);
	    }

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("")));

	    logger.debug("[GEMEventHandler preparingTTSTestModeActionAction] preparingTestModeAction Executed");
	}
    }

    public void testingTTSAction(Object obj) throws UserActionException {

	if (obj instanceof StateNotification) {

	    // triggered by State Notification from child resource

	    /************************************************
	     * PUT HERE YOUR CODE
	     ***********************************************/

	    return;
	} else if (obj instanceof StateEnteredEvent) {
	    System.out.println("Executing testingTTSAction");
	    logger.info("[GEMEventHandler testingTTSAction] Executing testingTTSAction");

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("testing TTS")));

	    // get the parameters of the command
	    ParameterSet<CommandParameter> parameterSet = getUserFunctionManager().getLastInput().getParameterSet();

	    // check parameter set
	    if (parameterSet.size()==0 || parameterSet.get(GEMParameters.TTS_TEST_FED_ID) == null ||
		parameterSet.get(GEMParameters.TTS_TEST_MODE) == null ||
		((StringT)parameterSet.get(GEMParameters.TTS_TEST_MODE).getValue()).equals("") ||
		parameterSet.get(GEMParameters.TTS_TEST_PATTERN) == null ||
		((StringT)parameterSet.get(GEMParameters.TTS_TEST_PATTERN).getValue()).equals("") ||
		parameterSet.get(GEMParameters.TTS_TEST_SEQUENCE_REPEAT) == null) {

                // go to error, we require parameters
                String errMsg = "testingTTSAction: no parameters given with TestTTS command.";

                // log error
                logger.error("[GEMEventHandler testingTTSAction] "+errMsg);

                // notify error
                sendCMSError(errMsg);

                //go to error state
                functionManager.fireEvent( GEMInputs.SETERROR );
            }

	    Integer  fedId = ((IntegerT)parameterSet.get(GEMParameters.TTS_TEST_FED_ID).getValue()).getInteger();
	    String    mode = ((StringT)parameterSet.get(GEMParameters.TTS_TEST_MODE).getValue()).getString();
	    String pattern = ((StringT)parameterSet.get(GEMParameters.TTS_TEST_PATTERN).getValue()).getString();
	    Integer cycles = ((IntegerT)parameterSet.get(GEMParameters.TTS_TEST_SEQUENCE_REPEAT).getValue()).getInteger();


	    // Set last parameters in the Function Manager parameters
	    functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.TTS_TEST_FED_ID,
                                                                                         new IntegerT(fedId)));
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.TTS_TEST_MODE,
                                                                                        new StringT(mode)));
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.TTS_TEST_PATTERN,
                                                                                        new StringT(pattern)));
	    functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.TTS_TEST_SEQUENCE_REPEAT,
                                                                                         new IntegerT(cycles)));

	    // debug
	    logger.debug("[GEMEventHandler testingTTSAction] Using parameters: fedId=" + fedId+
                         "mode=" + mode + " pattern=" + pattern + " cycles=" + cycles );

	    // find out which application controls the fedId.

	    int crate, slot, bits, repeat;

	    int fedID = ((IntegerT)parameterSet.get(GEMParameters.TTS_TEST_FED_ID).getValue()).getInteger();
	    crate = getCrateNumber(fedID);
	    slot  = getSlotNumber(fedID);
	    bits  = Integer.parseInt(((StringT)parameterSet.get(GEMParameters.TTS_TEST_PATTERN)
                                      .getValue()).getString());
	    if (((StringT)parameterSet.get(GEMParameters.TTS_TEST_PATTERN)
		 .getValue()).getString().equals("PATTERN")) {
		repeat = 0;
	    } else {  // CYCLE
		repeat = ((IntegerT)parameterSet.get(GEMParameters.TTS_TEST_SEQUENCE_REPEAT).getValue()).getInteger();
	    }

	    // leave intermediate state
	    functionManager.fireEvent( GEMInputs.SETTTSTEST_MODE );

	    // set action
	    functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("")));

	    logger.debug("[GEMEventHandler testingTTSAction] testingTTSAction Executed");
	}
    }

    @SuppressWarnings("unchecked")
	private void sendCMSError(String errMessage){

	// create a new error notification msg
	CMSError error = functionManager.getErrorFactory().getCMSError();
	error.setDateTime(new Date().toString());
	error.setMessage(errMessage);

	// update error msg parameter for GUI
	functionManager.getParameterSet().get(GEMParameters.ERROR_MSG).setValue(new StringT(errMessage));

	// send error
	try {
	    functionManager.getParentErrorNotifier().sendError(error);
	} catch (Exception e) {
	    logger.warn("[GEMEventHandler sendCMSError] "+functionManager.getClass().toString()+
                        ": Failed to send error mesage " + errMessage);
	}
    }

    public void coldResettingAction(Object obj) throws UserActionException {

        if (obj instanceof StateNotification) {

            // triggered by State Notification from child resource

            /************************************************
             * PUT YOUR CODE HERE
             ***********************************************/

            return;
        }

        else if (obj instanceof StateEnteredEvent) {
            System.out.println("Executing coldResettingAction");
            logger.info("Executing coldResettingAction");

            // set action
            functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("coldResetting")));

            /************************************************
             * PUT YOUR CODE HERE
             ***********************************************/
            // perform a cold-reset of your hardware

            functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("Cold Reset completed.")));
            // leave intermediate state
            functionManager.fireEvent( GEMInputs.SETHALTED );

            logger.debug("coldResettingAction Executed");
        }
    }

    public void fixSoftErrorAction(Object obj) throws UserActionException {

        if (obj instanceof StateNotification) {

            // triggered by State Notification from child resource

            /************************************************
             * PUT YOUR CODE HERE
             ***********************************************/

            return;
        }

        else if (obj instanceof StateEnteredEvent) {
            System.out.println("Executing fixSoftErrorAction");
            logger.info("Executing fixSoftErrorAction");

            // set action
            functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.ACTION_MSG,
                                                                                        new StringT("fixingSoftError")));

            // get the parameters of the command
            ParameterSet<CommandParameter> parameterSet = getUserFunctionManager().getLastInput().getParameterSet();

            // check parameter set
            Long triggerNumberAtPause = null;
            if (parameterSet.size()==0 || parameterSet.get(GEMParameters.TRIGGER_NUMBER_AT_PAUSE) == null) {

                // go to error, we require parameters
                String warnMsg = "fixSoftErrorAction: no parameters given with fixSoftError command.";

                // log error
                logger.warn(warnMsg);

            } else {
                triggerNumberAtPause = ((LongT)parameterSet.get(GEMParameters.TRIGGER_NUMBER_AT_PAUSE).getValue()).getLong();
            }

            /************************************************
             * PUT YOUR CODE HERE TO FIX THE SOFT ERROR
             ***********************************************/

            functionManager.setSoftErrorDetected(false);


            // if the soft error cannot be fixed, the FM should go to ERROR

            if (functionManager.hasSoftError())
                functionManager.fireEvent(  GEMInputs.SETERROR  );
            else
                functionManager.fireEvent(  functionManager.isDegraded() ? GEMInputs.SETRUNNINGDEGRADED : GEMInputs.SETRUNNING  );

            // Clean-up of the Function Manager parameters
            cleanUpFMParameters();

            logger.debug("resumeAction Executed");

        }
    }

    //
    // for testing with external inputs.
    //
    // Here we just set our DEGRADED/SOFTERROR state according to an external trigger that sent us to this state.
    // In a real FM, an external event or periodic check will trigger the FM to change state.
    //
    //
    public void runningDegradedAction(Object obj) throws UserActionException {
        if (obj instanceof StateEnteredEvent) {
            functionManager.setDegraded(true);
        }
    }

    //
    // for testing with external inputs
    //
    // Here we just set our DEGRADED/SOFTERROR state according to an external trigger that sent us to this state.
    // In a real FM, an external event or periodic check will trigger the FM to change state.
    //
    //
    public void runningSoftErrorDetectedAction(Object obj) throws UserActionException {
        if (obj instanceof StateEnteredEvent) {
            // do not touch degraded
            functionManager.setSoftErrorDetected(true);
        }
    }

    //
    // for testing with external inputs
    //
    // Here we just set our DEGRADED/SOFTERROR state according to an external trigger that sent us to this state.
    // In a real FM, an external event or periodic check will trigger the FM to change state.
    //
    //
    public void runningAction(Object obj) throws UserActionException {
        if (obj instanceof StateEnteredEvent) {
            functionManager.setDegraded(false);
            functionManager.setSoftErrorDetected(false);
        }
    }


    private void cleanUpFMParameters() {
	// Clean-up of the Function Manager parameters
	functionManager.getParameterSet().put(new FunctionManagerParameter<StringT >(GEMParameters.ACTION_MSG,
                                                                                     new StringT("")));
	functionManager.getParameterSet().put(new FunctionManagerParameter<StringT >(GEMParameters.ERROR_MSG,
                                                                                     new StringT("")));
	functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.RUN_NUMBER,
                                                                                     new IntegerT(-1)));
	functionManager.getParameterSet().put(new FunctionManagerParameter<StringT >(GEMParameters.RUN_TYPE,
                                                                                     new StringT("")));
	functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.TTS_TEST_FED_ID,
                                                                                     new IntegerT(-1)));
	functionManager.getParameterSet().put(new FunctionManagerParameter<StringT >(GEMParameters.TTS_TEST_MODE,
                                                                                     new StringT("")));
	functionManager.getParameterSet().put(new FunctionManagerParameter<StringT >(GEMParameters.TTS_TEST_PATTERN,
                                                                                     new StringT("")));
	functionManager.getParameterSet().put(new FunctionManagerParameter<IntegerT>(GEMParameters.TTS_TEST_SEQUENCE_REPEAT,
                                                                                     new IntegerT(-1)));
    }

    public void publishRunInfo(boolean doRun)
    {
	RunInfoConnectorIF ric = functionManager.getRunInfoConnector();

	if ( ric != null ) {
            if ( runNumberData != null ) {
                runInfo = new RunInfo(ric,runNumberData);
            } else {
                runInfo = new RunInfo(ric,runNumber);
            }

            runInfo.setNameSpace(GEMParameters.GEM_NS);
            if ( localRunNumber ) {
                try {
                    // deprecated use publish(parameter)
                    runInfo.publishRunInfo("RunNumber", runNumber.toString());
                    // runInfo.publish(runNumber);
                } catch ( RunInfoException e ) {
                    logger.error("[GEMEventHandler publishRunInfo] RunInfoException: "+e.toString());
                }
            }
        }
    }
    // Record LhcStatus info
    /*
      try {
      lhcStatus = LhcStatus.fetchLhcStatus();
      Class c = lhcStatus.getClass();
      Field fields [] = c.getFields();
      for ( int i=0; i<fields.length; i++) {
      String fieldName = fields[i].getName();
      Object value = fields[i].get(lhcStatus);
      String type = fields[i].getType().getName();

      if ( type.equals("java.lang.String") ) {
      StringT sValue = new StringT((String)value);
      Parameter<StringT> p = new Parameter<StringT>(fieldName,sValue);
      runInfo.publishWithHistory(p);
      } else if ( type.equals("int" ) ) {
      IntegerT iValue = new IntegerT((Integer)value);
      Parameter<IntegerT> p = new Parameter<IntegerT>(fieldName,iValue);
      runInfo.publishWithHistory(p);
      }
      }
      } catch ( Exception e ) {
      logger.warn("[GEMEventHandler publishRunInfo] "+e.toString());
      }
      } else {
      logger.error("[GEMEventHandler publishRunInfo] Cannot find RunInfoConnectorIF");
      }
      }
    */

    private void destroyXDAQ() throws UserActionException {
	functionManager.getParameterSet().put(new FunctionManagerParameter<StringT>(GEMParameters.STATE,
                                                                                    new StringT(functionManager.getState()
                                                                                                .getStateString())));

	// get executives and their job controls
	List<QualifiedResource> listExecutive = qualifiedGroup
	    .seekQualifiedResourcesOfType(new XdaqExecutive());

	for (QualifiedResource ex : listExecutive) {
	    ((XdaqExecutive)ex).killMe();
	    logger.debug("[GEMEventHandler destroyxDAQ] killMe() called on " + ex.getResource().getURL());
	}
    }

    private void killAllXDAQ() throws UserActionException {
	// get executives and their job controls
	List<QualifiedResource> listJC = qualifiedGroup
	    .seekQualifiedResourcesOfType(new JobControl());

	for (QualifiedResource jc : listJC) {
	    ((JobControl)jc).killAll();
	    logger.debug("[GEMEventHandler killAllxDAQ] killAll() called on " + jc.getResource().getURL());
	}
    }

    /* */
    private int getCrateNumber(int fed) {
	int crate = 1;

	if (fed == 760) {
	    crate = 3; // TF-FED
	} else if (fed == 752 || (fed / 10) == 83) {
	    crate = 2;
	}

	return crate;
    }

    /* */
    private int getSlotNumber(int fed) {
        // all AMC13s in slot 13
	int slot = 13;

	switch (fed) {
	case 760: slot = 13; break; // TF

	case 750: slot = 13; break; // plus 1

	case 752: slot = 13; break; // plus 2
	}

	return slot;
    }
}

// End of file
