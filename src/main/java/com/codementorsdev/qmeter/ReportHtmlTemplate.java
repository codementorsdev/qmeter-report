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
                </script>
                <script src="js/utils.js"></script>
                <script src="js/report-app.js"></script>
            </body>
            </html>
            """;
    }
}