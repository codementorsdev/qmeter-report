
package com.codementorsdev.qmeter;

public class ReportHtmlTemplate {
    public static String getHtmlTemplate() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Test Automation Report</title>
                <script src="https://unpkg.com/react@17/umd/react.production.min.js"></script>
                <script src="https://unpkg.com/react-dom@17/umd/react-dom.production.min.js"></script>
                <script src="https://unpkg.com/recharts/umd/Recharts.js"></script>
                <script src="https://unpkg.com/lucide@latest/dist/umd/lucide.min.js"></script>
                <script src="https://cdn.tailwindcss.com"></script>
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
                <style>
                    body { font-family: 'Inter', sans-serif; }
                </style>
            </head>
            <body>
                <div id="root"></div>
                <script>
                    const REPORT_DATA = %s;
                    const generateUUID=()=>'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,function(c){const r=Math.random()*16|0,v=c==='x'?r:(r&0x3|0x8);return v.toString(16)});
                    const formatDuration=ms=>{if(ms<1e3)return`${ms}ms`;const seconds=Math.floor(ms/1e3);const minutes=Math.floor(seconds/60);const hours=Math.floor(minutes/60);const remainingSeconds=seconds%%60;const remainingMinutes=minutes%%60;let parts=[];if(hours>0)parts.push(`${hours}h`);if(remainingMinutes>0)parts.push(`${remainingMinutes}m`);if(remainingSeconds>0||parts.length===0)parts.push(`${remainingSeconds}s`);return parts.join(' ')};
                    const formatDateTime=date=>new Date(date).toLocaleString('en-US',{year:'numeric',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit',second:'2-digit',hour12:!0});
                    const STATUS_COLORS={'Pass':'#4CAF50','Fail':'#F44336','Skip':'#FFC107','Error':'#9C27B0','Total':'#2196F3'};

                    // Wait for all dependencies to load
                    window.onload = function() {
                        const App = () => {
                            // ... rest of your React app code ...
                        };
                        ReactDOM.render(React.createElement(App), document.getElementById('root'));
                    };
                </script>
            </body>
            </html>
            """;
    }
}
