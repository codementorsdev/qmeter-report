
const App = () => {
  const [reportData, setReportData] = React.useState(null);
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filterStatus, setFilterStatus] = React.useState('All');
  const [filterEnvironment, setFilterEnvironment] = React.useState('All');
  const [filterPlatform, setFilterPlatform] = React.useState('All');
  
  const [expandedSuites, setExpandedSuites] = React.useState({});
  const [expandedTestCases, setExpandedTestCases] = React.useState({});
  const [showFilters, setShowFilters] = React.useState(false);

  const toggleSuiteExpansion = React.useCallback((suiteId) => {
    setExpandedSuites(prev => ({ ...prev, [suiteId]: !prev[suiteId] }));
  }, []);

  const toggleTestCaseExpansion = React.useCallback((testCaseId) => {
    setExpandedTestCases(prev => ({ ...prev, [testCaseId]: !prev[testCaseId] }));
  }, []);

  React.useEffect(() => {
    setReportData(window.REPORT_DATA);
  }, []);

  // ... rest of your React component code ...

  if (!reportData) {
    return <div>Loading...</div>;
  }

  return <div>Report content here...</div>;
};

ReactDOM.render(React.createElement(App), document.getElementById('root'));
