
const generateUUID = () => 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
});

const formatDuration = (ms) => {
    if (ms < 1000) return `${ms}ms`;
    const seconds = Math.floor(ms/1000);
    const minutes = Math.floor(seconds/60);
    const hours = Math.floor(minutes/60);
    const remainingSeconds = seconds % 60;
    const remainingMinutes = minutes % 60;
    let parts = [];
    if (hours > 0) parts.push(`${hours}h`);
    if (remainingMinutes > 0) parts.push(`${remainingMinutes}m`);
    if (remainingSeconds > 0 || parts.length === 0) parts.push(`${remainingSeconds}s`);
    return parts.join(' ');
};

const formatDateTime = (date) => new Date(date).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: true
});

const STATUS_COLORS = {
    'Pass': '#4CAF50',
    'Fail': '#F44336',
    'Skip': '#FFC107',
    'Error': '#9C27B0',
    'Total': '#2196F3'
};
