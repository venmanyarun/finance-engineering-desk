import React, { useState } from 'react';
import { useFinance } from './FinanceContext';

export function ImportExport() {
    const { getAuthHeaders } = useFinance();
    const [status, setStatus] = useState('');

    const exportToLocal = async () => {
        const res = await fetch('http://localhost:8080/api/finance/export', { headers: getAuthHeaders() });
        const data = await res.json();
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `finance_backup_${new Date().toISOString().split('T')[0]}.json`;
        a.click();
    };

    const downloadExcelReport = async () => {
        setStatus('Generating Excel Report...');
        try {
            const res = await fetch('http://localhost:8080/api/finance/export-excel', { 
                headers: getAuthHeaders() 
            });
            if (res.ok) {
                const blob = await res.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `Financial_Summary_${new Date().toISOString().split('T')[0]}.xlsx`;
                document.body.appendChild(a);
                a.click();
                a.remove();
                setStatus('Report Downloaded Successfully');
            } else {
                setStatus('Failed to generate report.');
            }
        } catch (err) {
            setStatus('Error connecting to reporting service.');
        }
    };

    const handleImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const text = await file.text();
        const res = await fetch('http://localhost:8080/api/finance/import', {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: text
        });
        if (res.ok) {
            alert('Import Successful');
            window.location.reload();
        }
    };

    return (
        <div className="workspace-panel" style={{padding: '30px', borderRadius: '12px', background: '#fff', border: '1px solid #e2e8f0'}}>
            <h3 style={{color: '#1e3a8a', marginBottom: '20px'}}>Data Management & Reporting</h3>
            
            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px'}}>
                <div style={{padding: '20px', background: '#f8fafc', borderRadius: '8px'}}>
                    <h4>Export Operations</h4>
                    <div style={{display:'flex', flexDirection:'column', gap:'10px', marginTop:'15px'}}>
                        <button className="btn-primary" onClick={downloadExcelReport} style={{backgroundColor: '#107c10'}}>
                            Download Full Excel Report (.xlsx)
                        </button>
                        <button className="btn-secondary" onClick={exportToLocal}>
                            Download JSON Backup
                        </button>
                    </div>
                </div>

                <div style={{padding: '20px', background: '#f8fafc', borderRadius: '8px'}}>
                    <h4>Import Operations</h4>
                    <p style={{fontSize: '12px', color: '#64748b', marginBottom: '15px'}}>Restore from a previously exported JSON backup.</p>
                    <input type="file" onChange={handleImport} style={{fontSize: '13px'}} />
                </div>
            </div>

            {status && <p style={{marginTop: '20px', fontWeight: '600', color: '#1e3a8a'}}>{status}</p>}
        </div>
    );
}