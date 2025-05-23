package com.codementorsdev.qmeter;

// Corrected Imports for Model classes
import com.codementorsdev.qmeter.model.ReportData;
import com.codementorsdev.qmeter.model.TestCase;
import com.codementorsdev.qmeter.model.TestEvent;
import com.codementorsdev.qmeter.model.TestStep;
import com.codementorsdev.qmeter.model.TestSuite;
import com.codementorsdev.qmeter.model.Summary; // Import Summary class

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReportGenerator {

    private static ReportGenerator instance;
    private final List<TestSuite> suites;
    private final ConcurrentMap<String, TestSuite> currentSuiteMap; // Map to handle concurrent suite additions
    final ConcurrentMap<String, TestCase> currentTestCaseMap; // Map to handle concurrent test case additions

    private ReportConfig config;
    private long overallStartTime;
    private long overallEndTime;

    private ReportGenerator() {
        this.suites = new ArrayList<>();
        this.currentSuiteMap = new ConcurrentHashMap<>();
        this.currentTestCaseMap = new ConcurrentHashMap<>();
        this.overallStartTime = System.currentTimeMillis();
    }

    /**
     * Initializes the ReportGenerator instance. This should ideally be called once at the start of your test run.
     * @param config The configuration for the report.
     */
    public static synchronized void initialize(ReportConfig config) {
        if (instance == null) {
            instance = new ReportGenerator();
            instance.config = config;
            System.out.println("ReportGenerator initialized with output: " + config.getOutputDirectory().resolve(config.getReportFileName()));
        } else {
            System.out.println("ReportGenerator already initialized.");
        }
    }

    /**
     * Get the singleton instance of ReportGenerator.
     * Throws an IllegalStateException if initialize() has not been called.
     * @return The singleton instance.
     */
    public static ReportGenerator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ReportGenerator has not been initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Starts a new test suite.
     * @param name The name of the test suite.
     * @return The ID of the newly started test suite.
     */
    public String startSuite(String name) { // <--- Changed return type to String
        TestSuite suite = new TestSuite();
        suite.setId(java.util.UUID.randomUUID().toString());
        suite.setName(name);
        suite.setStartTime(System.currentTimeMillis());
        suite.setTestCases(new ArrayList<>()); // Initialize empty list for test cases
        currentSuiteMap.put(suite.getId(), suite);
        suites.add(suite); // Add to the main list
        System.out.println("Started suite: " + name + " (ID: " + suite.getId() + ")"); // Added ID to log
        return suite.getId(); // <--- Return the generated ID
    }

    /**
     * Ends the current test suite.
     * @param suiteId The ID of the test suite to end.
     */
    public void endSuite(String suiteId) {
        TestSuite suite = currentSuiteMap.remove(suiteId);
        if (suite != null) {
            suite.setEndTime(System.currentTimeMillis());
            suite.setDuration(suite.getEndTime() - suite.getStartTime());

            // Recalculate suite status based on contained test cases
            boolean failed = suite.getTestCases().stream().anyMatch(tc -> "Fail".equals(tc.getStatus()) || "Error".equals(tc.getStatus()));
            suite.setStatus(failed ? "Fail" : "Pass");
            System.out.println("Ended suite: " + suite.getName() + " Status: " + suite.getStatus());
        } else {
            System.err.println("Could not find suite with ID: " + suiteId + " to end.");
        }
    }

    /**
     * Starts a new test case within the current suite.
     * @param suiteId The ID of the suite this test case belongs to.
     * @param name The name of the test case.
     * @param description A brief description of the test case.
     * @param environment The environment where the test is running.
     * @param platform The platform where the test is running.
     * @return The ID of the newly started test case.
     */
    public String startTestCase(String suiteId, String name, String description, String environment, String platform) {
        TestSuite suite = currentSuiteMap.get(suiteId);
        if (suite == null) {
            System.err.println("Cannot start test case '" + name + "': Suite with ID " + suiteId + " not found.");
            return null;
        }

        TestCase testCase = new TestCase(name, description, environment, platform);
        testCase.setSteps(new ArrayList<>());
        testCase.setLogs(new ArrayList<>());
        testCase.setEvents(new ArrayList<>());
        currentTestCaseMap.put(testCase.getId(), testCase);
        suite.getTestCases().add(testCase);
        System.out.println("  Started test case: " + name);
        return testCase.getId();
    }

    /**
     * Ends a test case.
     * @param testCaseId The ID of the test case to end.
     * @param status The final status of the test case (Pass, Fail, Skip, Error).
     */
    public void endTestCase(String testCaseId, String status) {
        TestCase testCase = currentTestCaseMap.remove(testCaseId);
        if (testCase != null) {
            testCase.end(status);
            System.out.println("  Ended test case: " + testCase.getName() + " Status: " + status);
        } else {
            System.err.println("Could not find test case with ID: " + testCaseId + " to end.");
        }
    }

    /**
     * Adds a step to the specified test case.
     * @param testCaseId The ID of the test case.
     * @param description The description of the step.
     * @param status The status of the step (e.g., "Pass", "Fail").
     * @param duration The duration of the step in milliseconds.
     */
    public void addStep(String testCaseId, String description, String status, long duration) {
        TestCase testCase = currentTestCaseMap.get(testCaseId);
        if (testCase != null) {
            if (testCase.getSteps() == null) testCase.setSteps(new ArrayList<>());
            testCase.getSteps().add(new TestStep(description, status, duration));
        } else {
            System.err.println("Test case with ID " + testCaseId + " not found. Cannot add step.");
        }
    }

    /**
     * Adds a log message to the specified test case.
     * @param testCaseId The ID of the test case.
     * @param message The log message.
     */
    public void addLog(String testCaseId, String message) {
        TestCase testCase = currentTestCaseMap.get(testCaseId);
        if (testCase != null) {
            if (testCase.getLogs() == null) testCase.setLogs(new ArrayList<>());
            testCase.getLogs().add(message);
        } else {
            System.err.println("Test case with ID " + testCaseId + " not found. Cannot add log.");
        }
    }

    /**
     * Adds an event to the specified test case.
     * @param testCaseId The ID of the test case.
     * @param type The type of event (e.g., "Info", "Warning", "Error").
     * @param message The event message.
     */
    public void addEvent(String testCaseId, String type, String message) {
        TestCase testCase = currentTestCaseMap.get(testCaseId);
        if (testCase != null) {
            if (testCase.getEvents() == null) testCase.setEvents(new ArrayList<>());
            testCase.getEvents().add(new TestEvent(System.currentTimeMillis(), type, message));
        } else {
            System.err.println("Test case with ID " + testCaseId + " not found. Cannot add event.");
        }
    }


    /**
     * Generates and flushes the HTML report to the configured directory.
     * This method should be called once after all tests have completed.
     */
    public void flushReport() {
        overallEndTime = System.currentTimeMillis();
        long totalExecutionTime = overallEndTime - overallStartTime;

        int totalTestCases = 0;
        int pass = 0;
        int fail = 0;
        int skip = 0;
        int error = 0;
        long minStartTime = Long.MAX_VALUE;
        long maxEndTime = Long.MIN_VALUE;

        // Ensure all suites and test cases are finalized
        for (TestSuite suite : suites) {
            // Re-calculate suite metrics to ensure they are up-to-date
            // The TestSuite's getters should internally handle calculation of its status/duration if needed
            // For now, we iterate over test cases to build the overall summary
            for (TestCase tc : suite.getTestCases()) {
                totalTestCases++;
                switch (tc.getStatus()) {
                    case "Pass": pass++; break;
                    case "Fail": fail++; break;
                    case "Skip": skip++; break;
                    case "Error": error++; break;
                }
                if (tc.getStartTime() < minStartTime) minStartTime = tc.getStartTime();
                if (tc.getEndTime() > maxEndTime) maxEndTime = tc.getEndTime();
            }
        }

        // Handle cases where no tests ran or start/end times are default
        if (minStartTime == Long.MAX_VALUE) minStartTime = overallStartTime;
        if (maxEndTime == Long.MIN_VALUE) maxEndTime = overallEndTime;

        Summary summary = new Summary(
                suites.size(),
                totalTestCases,
                pass,
                fail,
                skip,
                error,
                totalExecutionTime,
                minStartTime,
                maxEndTime,
                config.getEnvironment(),
                config.getPlatform()
        );

        ReportData reportData = new ReportData(suites, summary);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty printing JSON

        try {
            String reportJson = mapper.writeValueAsString(reportData);
            // FIX: Replace the correct placeholder with the actual JSON data
            String htmlContent = ReportHtmlTemplate.getHtmlTemplate()
                    .replace("window.REPORT_DATA = ;", "window.REPORT_DATA = " + reportJson + ";")
                    .replace("%s", """
            const generateUUID=()=>'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,function(c){const r=Math.random()*16|0,v=c==='x'?r:(r&0x3|0x8);return v.toString(16)});
            const formatDuration=ms=>{if(ms<1e3)return`${ms}ms`;const seconds=Math.floor(ms/1e3);const minutes=Math.floor(seconds/60);const hours=Math.floor(minutes/60);const remainingSeconds=seconds%60;const remainingMinutes=minutes%60;let parts=[];if(hours>0)parts.push(`${hours}h`);if(remainingMinutes>0)parts.push(`${remainingMinutes}m`);if(remainingSeconds>0||parts.length===0)parts.push(`${remainingSeconds}s`);return parts.join(' ')};
            const formatDateTime=date=>new Date(date).toLocaleString('en-US',{year:'numeric',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit',second:'2-digit',hour12:!0});
            const STATUS_COLORS={'Pass':'#4CAF50','Fail':'#F44336','Skip':'#FFC107','Error':'#9C27B0','Total':'#2196F3'};
            const App=()=>{const[reportData,setReportData]=React.useState(null);const[searchTerm,setSearchTerm]=React.useState('');const[filterStatus,setFilterStatus]=React.useState('All');const[filterEnvironment,setFilterEnvironment]=React.useState('All');const[filterPlatform,setFilterPlatform]=React.useState('All');const[expandedSuites,setExpandedSuites,toggleSuiteExpansion]=React.useState({}),React.useCallback(suiteId=>{setExpandedSuites(prev=>({...prev,[suiteId]:!prev[suiteId]}))},[]);const[expandedTestCases,setExpandedTestCases,toggleTestCaseExpansion]=React.useState({}),React.useCallback(testCaseId=>{setExpandedTestCases(prev=>({...prev,[testCaseId]:!prev[testCaseId]}))},[]);const[showFilters,setShowFilters]=React.useState(!1);React.useEffect(()=>{setReportData(window.REPORT_DATA);},[]);const handleRefreshReport=()=>{setReportData(window.REPORT_DATA);setSearchTerm('');setFilterStatus('All');setFilterEnvironment('All');setFilterPlatform('All');setExpandedSuites({});setExpandedTestCases({});};
            const allEnvironments=React.useMemo(()=>{if(!reportData)return[];const envs=new Set();reportData.suites.forEach(suite=>suite.testCases.forEach(tc=>envs.add(tc.environment)));return['All',...Array.from(envs).sort()]},[reportData]);
            const allPlatforms=React.useMemo(()=>{if(!reportData)return[];const plats=new Set();reportData.suites.forEach(suite=>suite.testCases.forEach(tc=>plats.add(tc.platform)));return['All',...Array.from(plats).sort()]},[reportData]);
            const filteredSuites=React.useMemo(()=>{if(!reportData)return[];return reportData.suites.map(suite=>{const filteredTestCases=suite.testCases.filter(testCase=>{const matchesSearch=testCase.name.toLowerCase().includes(searchTerm.toLowerCase())||testCase.description.toLowerCase().includes(searchTerm.toLowerCase())||testCase.steps.some(step=>step.description.toLowerCase().includes(searchTerm.toLowerCase()));const matchesStatus=filterStatus==='All'||testCase.status===filterStatus;const matchesEnvironment=filterEnvironment==='All'||testCase.environment===filterEnvironment;const matchesPlatform=filterPlatform==='All'||testCase.platform===filterPlatform;return matchesSearch&&matchesStatus&&matchesEnvironment&&matchesPlatform;});if(filteredTestCases.length===0&&(searchTerm||filterStatus!=='All'||filterEnvironment!=='All'||filterPlatform!=='All')){return null;}return{...suite,testCases:filteredTestCases};}).filter(Boolean);},[reportData,searchTerm,filterStatus,filterEnvironment,filterPlatform]);
            const copyToClipboard=(text,message='Copied to clipboard!')=>{navigator.clipboard.writeText(text).then(()=>{console.log(message);}).catch(err=>{console.error('Failed to copy: ',err);});};
            if(!reportData){return React.createElement('div',{className:'min-h-screen flex items-center justify-center bg-gray-100 font-inter text-gray-800'},React.createElement('div',{className:'flex items-center space-x-2'},React.createElement('div',{className:'w-4 h-4 border-2 border-t-2 border-gray-900 border-solid rounded-full animate-spin'}),React.createElement('div',null,'Loading Report...')));}
            const{summary}=reportData;
            const statusData= [{name:'Passed',value:summary.pass},{name:'Failed',value:summary.fail},{name:'Skipped',value:summary.skip},{name:'Errors',value:summary.error}].filter(item=>item.value>0);
            const suiteExecutionData=filteredSuites.map(suite=>({name:suite.name,duration:suite.duration,})).sort((a,b)=>b.duration-a.duration);
            const totalFilteredTestCases=filteredSuites.reduce((sum,suite)=>sum+suite.testCases.length,0);
            const filteredPass=filteredSuites.reduce((sum,suite)=>sum+suite.testCases.filter(tc=>tc.status==='Pass').length,0);
            const filteredFail=filteredSuites.reduce((sum,suite)=>sum+suite.testCases.filter(tc=>tc.status==='Fail').length,0);
            const filteredSkip=filteredSuites.reduce((sum,suite)=>sum+suite.testCases.filter(tc=>tc.status==='Skip').length,0);
            const filteredError=filteredSuites.reduce((sum,suite)=>sum+suite.testCases.filter(tc=>tc.status==='Error').length,0);
            const filteredStatusData=[{name:'Passed',value:filteredPass},{name:'Failed',value:filteredFail},{name:'Skipped',value:filteredSkip},{name:'Errors',value:filteredError}].filter(item=>item.value>0);
            return React.createElement('div',{className:'min-h-screen bg-gradient-to-br from-gray-50 to-gray-200 font-inter text-gray-800 p-6 sm:p-8'},React.createElement('div',{className:'max-w-7xl mx-auto bg-white shadow-xl rounded-2xl overflow-hidden border border-gray-200'},React.createElement('header',{className:'bg-gradient-to-r from-blue-600 to-indigo-700 text-white p-6 sm:p-8 flex flex-col sm:flex-row justify-between items-start sm:items-center rounded-t-2xl'},React.createElement('div',null,React.createElement('h1',{className:'text-3xl sm:text-4xl font-extrabold mb-2'},'Test Automation Execution Report'),React.createElement('p',{className:'text-blue-200 text-lg'},'Comprehensive insights into your test runs')),React.createElement('button',{onClick:handleRefreshReport,className:'mt-4 sm:mt-0 px-5 py-2 bg-blue-500 hover:bg-blue-600 rounded-lg text-white font-semibold transition duration-300 ease-in-out flex items-center shadow-md'},React.createElement(lucide_react.RefreshCcwIcon,{size:18,className:'mr-2'}),' Regenerate Report')),React.createElement('section',{className:'p-6 sm:p-8 border-b border-gray-200'},React.createElement('h2',{className:'text-2xl font-bold mb-6 text-gray-900'},'Overall Summary'),React.createElement('div',{className:'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8'},React.createElement('div',{className:'bg-gray-50 p-5 rounded-xl shadow-sm border border-gray-200'},React.createElement('p',{className:'text-sm font-medium text-gray-500'},'Total Test Cases'),React.createElement('p',{className:'text-3xl font-bold text-gray-900 mt-1'},summary.totalTestCases)),React.createElement('div',{className:'bg-gray-50 p-5 rounded-xl shadow-sm border border-gray-200'},React.createElement('p',{className:'text-sm font-medium text-gray-500'},'Total Suites'),React.createElement('p',{className:'text-3xl font-bold text-gray-900 mt-1'},summary.totalSuites)),React.createElement('div',{className:'bg-gray-50 p-5 rounded-xl shadow-sm border border-gray-200'},React.createElement('p',{className:'text-sm font-medium text-gray-500'},'Total Execution Time'),React.createElement('p',{className:'text-3xl font-bold text-gray-900 mt-1'},formatDuration(summary.totalExecutionTime))),React.createElement('div',{className:'bg-gray-50 p-5 rounded-xl shadow-sm border border-gray-200'},React.createElement('p',{className:'text-sm font-medium text-gray-500'},'Start Time'),React.createElement('p',{className:'text-lg font-bold text-gray-900 mt-1'},formatDateTime(summary.startTime)),React.createElement('p',{className:'text-sm font-medium text-gray-500 mt-2'},'End Time'),React.createElement('p',{className:'text-lg font-bold text-gray-900 mt-1'},formatDateTime(summary.endTime)))),React.createElement('div',{className:'grid grid-cols-1 lg:grid-cols-2 gap-8'},statusData.length>0&&React.createElement('div',{className:'bg-gray-50 p-6 rounded-xl shadow-sm border border-gray-200 flex flex-col items-center'},React.createElement('h3',{className:'text-xl font-semibold mb-4 text-gray-800'},'Test Case Status Distribution'),React.createElement(recharts.ResponsiveContainer,{width:'100%',height:300},React.createElement(recharts.PieChart,null,React.createElement(recharts.Pie,{data:statusData,cx:'50%',cy:'50%',outerRadius:100,fill:'#8884d8',dataKey:'value',labelLine:!1,label:({name,percent})=>`${name}: ${(percent*100).toFixed(0)}%`},statusData.map((entry,index)=>React.createElement(recharts.Cell,{key:`cell-${index}`,fill:STATUS_COLORS[entry.name.replace('ed','')]})),),React.createElement(recharts.Tooltip,{formatter:(value,name)=>[`${value} Test Cases`,name]}),React.createElement(recharts.Legend,null)))),suiteExecutionData.length>0&&React.createElement('div',{className:'bg-gray-50 p-6 rounded-xl shadow-sm border border-gray-200'},React.createElement('h3',{className:'text-xl font-semibold mb-4 text-gray-800'},'Top 5 Suites by Execution Time'),React.createElement(recharts.ResponsiveContainer,{width:'100%',height:300},React.createElement(recharts.BarChart,{data:suiteExecutionData.slice(0,5),margin:{top:5,right:30,left:20,bottom:5},layout:'vertical'},React.createElement(recharts.XAxis,{type:'number',tickFormatter:ms=>formatDuration(ms)}),React.createElement(recharts.YAxis,{type:'category',dataKey:'name',width:120}),React.createElement(recharts.Tooltip,{formatter:value=>formatDuration(value)}),React.createElement(recharts.Bar,{dataKey:'duration',fill:'#8884d8',radius:[10,10,0,0]})))))),React.createElement('section',{className:'p-6 sm:p-8 border-b border-gray-200 bg-gray-50'},React.createElement('div',{className:'flex justify-between items-center mb-4'},React.createElement('h2',{className:'text-2xl font-bold text-gray-900'},'Test Case Details'),React.createElement('button',{onClick:()=>setShowFilters(!showFilters),className:'px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg text-gray-700 font-semibold transition duration-300 ease-in-out flex items-center shadow-sm'},React.createElement(lucide_react.FilterIcon,{size:18,className:'mr-2'}),showFilters?'Hide Filters':'Show Filters')),showFilters&&React.createElement('div',{className:'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6 transition-all duration-300 ease-in-out origin-top scale-y-100 opacity-100'},React.createElement('div',{className:'relative'},React.createElement(lucide_react.SearchIcon,{className:'absolute left-3 top-1/2 -translate-y-1/2 text-gray-400',size:18}),React.createElement('input',{type:'text',placeholder:'Search test cases...',className:'w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500',value:searchTerm,onChange:e=>setSearchTerm(e.target.value)})),React.createElement('select',{className:'w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 appearance-none bg-white bg-no-repeat bg-[length:1.2rem_1.2rem] bg-[right_0.75rem_center]',style:{backgroundImage:`url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%236B7280' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E")`},value:filterStatus,onChange:e=>setFilterStatus(e.target.value)},React.createElement('option',{value:'All'},'All Statuses'),Object.keys(STATUS_COLORS).map(status=>status!=='Total'&&React.createElement('option',{key:status,value:status},status))),React.createElement('select',{className:'w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 appearance-none bg-white bg-no-repeat bg-[length:1.2rem_1.2rem] bg-[right_0.75rem_center]',style:{backgroundImage:`url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%236B7280' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E")`},value:filterEnvironment,onChange:e=>setFilterEnvironment(e.target.value)},allEnvironments.map(env=>React.createElement('option',{key:env,value:env},env))),React.createElement('select',{className:'w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 appearance-none bg-white bg-no-repeat bg-[length:1.2rem_1.2rem] bg-[right_0.75rem_center]',style:{backgroundImage:`url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%236B7280' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E")`},value:filterPlatform,onChange:e=>setFilterPlatform(e.target.value)},allPlatforms.map(plat=>React.createElement('option',{key:plat,value:plat},plat)))),totalFilteredTestCases>0&&React.createElement('div',{className:'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6'},React.createElement('div',{className:'p-3 rounded-lg bg-green-50 text-green-700 font-medium flex items-center justify-between'},'Passed: ',React.createElement('span',{className:'font-bold text-lg'},filteredPass)),React.createElement('div',{className:'p-3 rounded-lg bg-red-50 text-red-700 font-medium flex items-center justify-between'},'Failed: ',React.createElement('span',{className:'font-bold text-lg'},filteredFail)),React.createElement('div',{className:'p-3 rounded-lg bg-yellow-50 text-yellow-700 font-medium flex items-center justify-between'},'Skipped: ',React.createElement('span',{className:'font-bold text-lg'},filteredSkip)),React.createElement('div',{className:'p-3 rounded-lg bg-purple-50 text-purple-700 font-medium flex items-center justify-between'},'Errors: ',React.createElement('span',{className:'font-bold text-lg'},filteredError)))),React.createElement('section',{className:'p-6 sm:p-8'},filteredSuites.length===0?React.createElement('div',{className:'text-center text-gray-600 text-lg py-10'},'No test suites or test cases match your current filters.'):filteredSuites.map(suite=>React.createElement('div',{key:suite.id,className:'mb-8 bg-gray-50 rounded-xl shadow-md border border-gray-200 overflow-hidden'},React.createElement('div',{className:`flex justify-between items-center p-5 cursor-pointer transition-all duration-300 ${expandedSuites[suite.id]?'bg-blue-100':'bg-gray-100 hover:bg-gray-200'}`,onClick:()=>toggleSuiteExpansion(suite.id)},React.createElement('h3',{className:'text-xl font-semibold text-gray-900 flex items-center'},expandedSuites[suite.id]?React.createElement(lucide_react.ChevronDownIcon,{size:20,className:'mr-2 text-blue-600'}):React.createElement(lucide_react.ChevronRightIcon,{size:20,className:'mr-2 text-gray-600'}),suite.name,React.createElement('span',{className:`ml-3 px-3 py-1 text-xs font-bold rounded-full ${suite.status==='Pass'?'bg-green-200 text-green-800':'bg-red-200 text-red-800'}`},suite.status)),React.createElement('div',{className:'text-gray-600 text-sm'},React.createElement('span',{className:'mr-4'},'Cases: ',suite.testCases.length),React.createElement('span',null,'Duration: ',formatDuration(suite.duration)))),expandedSuites[suite.id]&&React.createElement('div',{className:'p-5 border-t border-gray-200'},suite.testCases.length===0?React.createElement('div',{className:'text-center text-gray-500 py-4'},'No test cases in this suite match the filters.'):React.createElement('div',{className:'space-y-4'},suite.testCases.map(testCase=>React.createElement('div',{key:testCase.id,className:'bg-white p-4 rounded-lg shadow-sm border border-gray-200'},React.createElement('div',{className:'flex justify-between items-center cursor-pointer',onClick:()=>toggleTestCaseExpansion(testCase.id)},React.createElement('div',{className:'flex items-center'},expandedTestCases[testCase.id]?React.createElement(lucide_react.ChevronDownIcon,{size:16,className:'mr-2 text-blue-500'}):React.createElement(lucide_react.ChevronRightIcon,{size:16,className:'mr-2 text-gray-500'}),React.createElement('span',{className:`font-medium ${testCase.status==='Pass'?'text-green-700':testCase.status==='Fail'?'text-red-700':testCase.status==='Skip'?'text-yellow-700':'text-purple-700'}`},testCase.status),React.createElement('span',{className:'ml-3 text-gray-900 font-semibold'},testCase.name)),React.createElement('div',{className:'text-gray-600 text-sm'},'Duration: ',formatDuration(testCase.duration))),expandedTestCases[testCase.id]&&React.createElement('div',{className:'mt-4 pt-4 border-t border-gray-100 space-y-3 text-sm text-gray-700'},React.createElement('p',null,React.createElement('strong',null,'Description:'),' ',testCase.description),React.createElement('p',null,React.createElement('strong',null,'Start Time:'),' ',formatDateTime(testCase.startTime)),React.createElement('p',null,React.createElement('strong',null,'End Time:'),' ',formatDateTime(testCase.endTime)),React.createElement('p',null,React.createElement('strong',null,'Environment:'),' ',testCase.environment),React.createElement('p',null,React.createElement('strong',null,'Platform:'),' ',testCase.platform),testCase.steps&&testCase.steps.length>0&&React.createElement('div',null,React.createElement('h4',{className:'font-semibold mt-4 mb-2 text-gray-800'},'Steps:'),React.createElement('ul',{className:'list-disc list-inside space-y-1'},testCase.steps.map(step=>React.createElement('li',{key:step.id,className:`${step.status==='Pass'?'text-green-600':'text-red-600'}`},React.createElement('span',{className:'font-bold'},step.status,':'),' ',step.description,' (',formatDuration(step.duration),')')))),testCase.logs&&testCase.logs.length>0&&React.createElement('div',null,React.createElement('h4',{className:'font-semibold mt-4 mb-2 text-gray-800 flex items-center'},'Logs:',React.createElement('button',{onClick:()=>copyToClipboard(testCase.logs.join('\\n'),'Logs copied!'),className:'ml-2 p-1 rounded-md hover:bg-gray-100 text-gray-500 hover:text-gray-700 transition',title:'Copy logs'},React.createElement(lucide_react.ClipboardIcon,{size:16}))),React.createElement('pre',{className:'bg-gray-100 p-3 rounded-lg text-xs overflow-x-auto max-h-40 whitespace-pre-wrap break-words border border-gray-200'},testCase.logs.join('\\n'))),testCase.events&&testCase.events.length>0&&React.createElement('div',null,React.createElement('h4',{className:'font-semibold mt-4 mb-2 text-gray-800'},'Events:'),React.createElement('ul',{className:'space-y-1'},testCase.events.map((event,idx)=>React.createElement('li',{key:idx,className:'text-xs'},React.createElement('span',{className:'font-mono text-gray-500 mr-2'},formatDateTime(event.timestamp)),React.createElement('span',{className:`font-bold ${event.type==='Error'?'text-red-600':event.type==='Warning'?'text-yellow-600':'text-blue-600'}`},'[',event.type,']'),' ',event.message))))))))),React.createElement('footer',{className:'bg-gray-800 text-white p-6 sm:p-8 text-center text-sm rounded-b-2xl'},React.createElement('p',null,'© ',new Date().getFullYear(),' Test Automation Report. All rights reserved.'),React.createElement('p',{className:'mt-2 text-gray-400'},'Generated with insights and precision.'))));};ReactDOM.render(React.createElement(App, null), document.getElementById('root'));
            """);

            Path outputPath = config.getOutputDirectory();
            Files.createDirectories(outputPath); // Create directories if they don't exist

            File outputFile = outputPath.resolve(config.getReportFileName()).toFile();
            FileUtils.writeStringToFile(outputFile, htmlContent, StandardCharsets.UTF_8);

            System.out.println("Test automation report generated successfully at: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Reset state for next potential run (if running multiple times in same JVM)
            instance = null;
        }
    }
}