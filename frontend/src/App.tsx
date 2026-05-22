import React, { useState, useEffect, useMemo } from 'react';
import { useFinance, FinanceProvider } from './FinanceContext';
import { ImportExport } from './ImportExport';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';

function FormField({ label, tooltip, children }) {
    return (
        <div style={{ marginBottom: '16px', position: 'relative' }}>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', color: '#475569', marginBottom: '4px' }}>
                {label}
                <span style={{ marginLeft: '6px', color: '#94a3b8', cursor: 'help' }} title={tooltip}>ⓘ</span>
            </label>
            {children}
        </div>
    );
}

function AuthForms() {
    const { login, register } = useFinance();
    const [isLogin, setIsLogin] = useState(true);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isLogin) {
            const success = await login(username, password);
            if (!success) setMessage('Login failed. Check credentials.');
        } else {
            const success = await register(username, password);
            if (success) {
                setMessage('Registered! Now please login.');
                setIsLogin(true);
            } else {
                setMessage('Registration failed.');
            }
        }
    };

    return (
        <div className="workspace-container" style={{maxWidth: '420px', margin: '80px auto'}}>
            <div className="form-panel" style={{boxShadow: '0 8px 30px rgba(30,58,138,0.06)', borderRadius: '16px', padding: '36px'}}>
                <div style={{textAlign: 'center', marginBottom: '28px'}}>
                    <h2 className="workspace-title" style={{fontSize: '28px', color: '#1e3a8a'}}>Finance Desk</h2>
                    <p className="workspace-tagline" style={{marginTop: '6px'}}>Engineering-Grade Accounting Console</p>
                </div>
                <form onSubmit={handleSubmit} style={{display:'flex', flexDirection:'column', gap:'16px'}}>
                    <input type="text" placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} required minLength={3} style={{padding: '12px', borderRadius: '8px', border: '1px solid #cbd5e1'}} />
                    <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required minLength={6} style={{padding: '12px', borderRadius: '8px', border: '1px solid #cbd5e1'}} />
                    <button type="submit" className="btn-primary" style={{borderRadius: '8px', padding: '12px', fontWeight: '700', backgroundColor: '#1e3a8a'}}>
                        {isLogin ? 'Sign In' : 'Create Account'}
                    </button>
                    <button type="button" className="btn-secondary" onClick={() => { setIsLogin(!isLogin); setMessage(''); }} style={{background: 'transparent', color: '#475569', border: 'none', marginTop: '10px'}}>
                        {isLogin ? "Don't have an account? Register" : 'Already have an account? Login'}
                    </button>
                    {message && <p style={{color: '#ef4444', textAlign:'center', fontSize: '14px'}}>{message}</p>}
                </form>
            </div>
        </div>
    );
}

function ManualTransactionForm({ accounts, todayStr, saveManualTransaction }) {
    return (
        <div className="form-panel">
            <h3>➕ Manual Transaction</h3>
            <form onSubmit={async (e) => {
                e.preventDefault();
                const data = Object.fromEntries(new FormData(e.target));
                await saveManualTransaction(data);
                e.target.reset();
            }}>
                <FormField label="Date" tooltip="Timestamp"><input type="date" name="date" required defaultValue={todayStr} /></FormField>
                <FormField label="Amount" tooltip="Value"><input type="number" step="0.01" name="amount" required min="0.01" /></FormField>
                <FormField label="Description" tooltip="Purpose"><input type="text" name="description" required /></FormField>
                <FormField label="Type" tooltip="Classification">
                    <select name="type">
                        <option value="EXPENSE">Expense</option>
                        <option value="INCOME">Income</option>
                        <option value="TRANSFER">Transfer</option>
                    </select>
                </FormField>
                <FormField label="Expense Category" tooltip="Optional classification for spend tracking">
                    <select name="category">
                        <option value="">Uncategorized</option>
                        <option value="FOOD">Food</option>
                        <option value="BILLS">Bills</option>
                        <option value="TRANSPORT">Transport</option>
                        <option value="HEALTH">Health</option>
                        <option value="ENTERTAINMENT">Entertainment</option>
                        <option value="RENT">Rent</option>
                        <option value="SHOPPING">Shopping</option>
                        <option value="GROCERIES">Groceries</option>
                        <option value="UTILITIES">Utilities</option>
                        <option value="HOUSEHOLD_EXPENSE">Household Expense</option>
                        <option value="LOAN_EMI">Loan EMI</option>
                        <option value="INVESTMENT_SIP">Investment SIP</option>
                        <option value="TAX_PAYMENT">Tax Payment</option>
                        <option value="SUBSCRIPTION">Subscription</option>
                        <option value="GUARANTEED_RETURN">Guaranteed Return</option>
                        <option value="ULIP">ULIP</option>
                        <option value="HEALTH_INSURANCE">Health Insurance</option>
                        <option value="LIFE_INSURANCE">Life Insurance</option>
                        <option value="VEHICLE_INSURANCE">Vehicle Insurance</option>
                        <option value="OTHER">Other</option>
                    </select>
                </FormField>
                <FormField label="Source Account" tooltip="Debit"><select name="sourceAccountId"><option value="">None</option>{accounts.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}</select></FormField>
                <FormField label="Destination Account" tooltip="Credit"><select name="destinationAccountId"><option value="">None</option>{accounts.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}</select></FormField>
                <button type="submit" className="btn-primary">Commit Transaction</button>
            </form>
        </div>
    );
}

function ConsoleDashboard() {
    const { 
        metrics, accounts, incomes, obligations, transactions, alerts, loading, user, logout, 
        saveAccount, removeAccount, saveIncome, removeIncome, saveObligation, removeObligation, 
        recordEvent, saveManualTransaction, removeTransaction, getAuthHeaders 
    } = useFinance();

    const [activeTab, setActiveTab] = useState('DASHBOARD');
    const [subTab, setSubTab] = useState('VIEW');
    const [editingItem, setEditingItem] = useState(null);
    const [categoryTab, setCategoryTab] = useState('ALL');
    const [txCategoryFilter, setTxCategoryFilter] = useState('ALL');
    const [forecast, setForecast] = useState([]);
    const [startMonth, setStartMonth] = useState('');
    const [endMonth, setEndMonth] = useState('');
    const [retirementProjection, setRetirementProjection] = useState(null);
    const [oblSortKey, setOblSortKey] = useState('instrumentName');

    const todayStr = new Date().toISOString().split('T')[0];

    useEffect(() => {
        if (activeTab === 'PROJECTIONS' || activeTab === 'RETIREMENT' || activeTab === 'DASHBOARD' || activeTab === 'INCOME' || activeTab === 'OBLIGATIONS') {
            const fetchForecast = async () => {
                const res = await fetch(`http://localhost:8080/api/finance/forecast?years=30`, { headers: getAuthHeaders() });
                if (res.ok) {
                    const data = await res.json();
                    setForecast(data);
                    if (data.length > 0 && !startMonth) {
                        setStartMonth(data[0].date);
                        setEndMonth(data[Math.min(11, data.length - 1)].date);
                    }
                }
            };
            fetchForecast();
        }
    }, [activeTab, user, getAuthHeaders, incomes, obligations, startMonth]);

    useEffect(() => {
        const fetchRetirementProjection = async () => {
            if (activeTab !== 'RETIREMENT' || !startMonth || !endMonth) {
                setRetirementProjection(null);
                return;
            }
            const res = await fetch(`http://localhost:8080/api/finance/retirement-projection?fromMonth=${startMonth}&toMonth=${endMonth}`, { headers: getAuthHeaders() });
            if (res.ok) {
                setRetirementProjection(await res.json());
            } else {
                setRetirementProjection(null);
            }
        };
        fetchRetirementProjection();
    }, [activeTab, startMonth, endMonth, getAuthHeaders, accounts, obligations, user]);

    const chartData = useMemo(() => {
        let cumulativeNetWorth = metrics.netWorth || 0;
        return (metrics.cashFlowForecast || []).map(f => {
            cumulativeNetWorth += (f.netInflow - f.netOutflow);
            return { name: f.date, netWorth: cumulativeNetWorth, outflow: f.netOutflow };
        });
    }, [metrics.cashFlowForecast, metrics.netWorth]);

    const rangeData = useMemo(() => {
        if (!startMonth || !endMonth || forecast.length === 0) return [];
        const startIndex = forecast.findIndex(f => f.date === startMonth);
        const endIndex = forecast.findIndex(f => f.date === endMonth);
        return forecast.slice(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex) + 1);
    }, [forecast, startMonth, endMonth]);

    const rangeMetrics = useMemo(() => {
        return rangeData.reduce((acc, curr) => {
            acc.totalInflow += (typeof curr.netInflow === 'number' ? curr.netInflow : parseFloat(curr.netInflow || 0));
            acc.totalOutflow += (typeof curr.netOutflow === 'number' ? curr.netOutflow : parseFloat(curr.netOutflow || 0));
            return acc;
        }, { totalInflow: 0, totalOutflow: 0 });
    }, [rangeData]);

    const selectionMonths = useMemo(() => (rangeData.length > 0 ? rangeData.length : 12), [rangeData]);

    const annualizedRangeMetrics = useMemo(() => {
        const months = selectionMonths || 12;
        const inflow = months ? (rangeMetrics.totalInflow / months) * 12 : 0;
        const outflow = months ? (rangeMetrics.totalOutflow / months) * 12 : 0;
        return { annualizedInflow: inflow, annualizedOutflow: outflow, annualizedSurplus: inflow - outflow };
    }, [rangeMetrics, selectionMonths]);

    const oneYearForecast = useMemo(() => {
        const source = metrics.cashFlowForecast && metrics.cashFlowForecast.length ? metrics.cashFlowForecast : forecast;
        return source.slice(0, Math.min(12, source.length));
    }, [metrics.cashFlowForecast, forecast]);

    const oneYearRangeLabel = useMemo(() => {
        if (oneYearForecast.length === 0) return 'next 12 months';
        return `${oneYearForecast[0].date} to ${oneYearForecast[oneYearForecast.length - 1].date}`;
    }, [oneYearForecast]);

    const twelveMonthInflowTotal = useMemo(() => {
        return oneYearForecast.reduce((sum, f) => sum + (typeof f.netInflow === 'number' ? f.netInflow : parseFloat(f.netInflow || 0)), 0);
    }, [oneYearForecast]);

    const twelveMonthOutflowTotal = useMemo(() => {
        return oneYearForecast.reduce((sum, f) => sum + (typeof f.netOutflow === 'number' ? f.netOutflow : parseFloat(f.netOutflow || 0)), 0);
    }, [oneYearForecast]);

    const sortedObligations = useMemo(() => {
        return [...obligations].sort((a, b) => {
            if (oblSortKey === 'amount') return b.amount - a.amount;
            if (oblSortKey === 'category') return a.category.localeCompare(b.category);
            if (oblSortKey === 'linkedAccount') return (a.linkedAccount?.name || '').localeCompare(b.linkedAccount?.name || '');
            return a.instrumentName.localeCompare(b.instrumentName);
        });
    }, [obligations, oblSortKey]);

    const filteredObligations = useMemo(() => {
        if (categoryTab === 'ALL') return sortedObligations;
        return sortedObligations.filter(o => o.category === categoryTab);
    }, [sortedObligations, categoryTab]);

    const categorySummary = useMemo(() => {
        const summary = { ALL: { count: obligations.length, annualized: 0 } };
        obligations.forEach(o => {
            if (!summary[o.category]) summary[o.category] = { count: 0, annualized: 0 };
            summary[o.category].count++;
            
            const amt = typeof o.amount === 'number' ? o.amount : parseFloat(o.amount || '0');
            const factor = o.frequency === 'MONTHLY' ? 12 : (o.frequency === 'QUARTERLY' ? 4 : 1);
            summary[o.category].annualized += (amt * factor);
            summary.ALL.annualized += (amt * factor);
        });
        return summary;
    }, [obligations]);

    const transactionCategorySummary = useMemo(() => {
        const summary = { ALL: { count: transactions.length, total: 0 } };
        transactions.forEach(tx => {
            const category = tx.category || 'UNCATEGORIZED';
            if (!summary[category]) summary[category] = { count: 0, total: 0 };
            summary[category].count++;
            summary[category].total += typeof tx.amount === 'number' ? tx.amount : parseFloat(tx.amount || '0');
            summary.ALL.total += typeof tx.amount === 'number' ? tx.amount : parseFloat(tx.amount || '0');
        });
        return summary;
    }, [transactions]);

    const filteredTransactions = useMemo(() => {
        if (txCategoryFilter === 'ALL') return transactions;
        return transactions.filter(tx => (tx.category || 'UNCATEGORIZED') === txCategoryFilter);
    }, [transactions, txCategoryFilter]);

    if (loading) return <div>Initialising Console...</div>;
    if (!user) return <AuthForms />;

    const renderSubTabs = () => (
        <div style={{display:'flex', gap:'10px', marginBottom:'20px', borderBottom:'1px solid #e2e8f0', paddingBottom:'10px'}}>
            <button className={subTab === 'VIEW' ? 'tab-btn active' : 'tab-btn'} onClick={() => {setSubTab('VIEW'); setEditingItem(null);}}>View Ledger</button>
            <button className={subTab === 'ADD' ? 'tab-btn active' : 'tab-btn'} onClick={() => {setSubTab('ADD'); setEditingItem(null);}}>+ Add New Entry</button>
        </div>
    );

    return (
        <div className="workspace-container" style={{padding: '24px 40px'}}>
            <header style={{display:'flex', justifyContent:'space-between', borderBottom: '1px solid #e2e8f0', paddingBottom: '24px', marginBottom: '32px'}}>
                <div>
                    <h1 style={{fontSize: '24px', color: '#1e3a8a'}}>📊 Financial Engineering Console</h1>
                    <p style={{color: '#64748b'}}>Operator: <strong>{user}</strong></p>
                </div>
                <button onClick={logout} className="action-btn delete">Logout</button>
            </header>

            <nav style={{display: 'flex', gap: '8px', marginBottom: '32px', flexWrap: 'wrap'}}>
                {['DASHBOARD', 'ACCOUNTS', 'INCOME', 'OBLIGATIONS', 'TRANSACTIONS', 'PROJECTIONS', 'RETIREMENT', 'DATA_MANAGEMENT'].map(tab => (
                    <button key={tab} className={activeTab === tab ? 'tab-btn active' : 'tab-btn'} onClick={() => {setActiveTab(tab); setSubTab('VIEW'); setEditingItem(null);}}>
                        {tab.charAt(0) + tab.slice(1).toLowerCase().replace('_', ' ')}
                    </button>
                ))}
            </nav>

            {activeTab === 'DASHBOARD' && (
                <div style={{display:'flex', flexDirection:'column', gap:'30px'}}>
                    <div className="dashboard-grid-matrix grid-four-col">
                        <div className="metric-display-panel" style={{borderLeft: '5px solid #10b981'}}>
                            <label className="panel-label">Current Assets</label>
                            <h3 className="panel-amount">₹{metrics.totalAssets?.toLocaleString('en-IN')}</h3>
                        </div>
                        <div className="metric-display-panel" style={{borderLeft: '5px solid #f43f5e'}}>
                            <label className="panel-label">Total Liabilities</label>
                            <h3 className="panel-amount">₹{metrics.totalLiabilities?.toLocaleString('en-IN')}</h3>
                        </div>
                        <div className="metric-display-panel" style={{background: '#1e3a8a', color:'white'}}>
                            <label className="panel-label-inverted" style={{color:'#93c5fd'}}>Current Net Worth</label>
                            <h3 className="panel-amount-inverted">₹{metrics.netWorth?.toLocaleString('en-IN')}</h3>
                        </div>
                        <div className="metric-display-panel" style={{borderLeft: '5px solid #8b5cf6'}}>
                            <label className="panel-label">Total Death Cover</label>
                            <h3 className="panel-amount">₹{metrics.totalDeathBenefit?.toLocaleString('en-IN')}</h3>
                        </div>
                    </div>

                    <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:'24px'}}>
                        <div className="metric-display-panel" style={{height:'400px'}}>
                            <label className="panel-label">Net Worth Growth Projection (12 Months)</label>
                            <div style={{fontSize:'12px', color:'#64748b', marginBottom:'8px'}}>Horizon: {oneYearRangeLabel}</div>
                            <ResponsiveContainer width="100%" height="82%">
                                <AreaChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 0 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                    <XAxis dataKey="name" fontSize={10} interval={0} />
                                    <YAxis fontSize={12} tickFormatter={(value) => `₹${(value / 100000).toFixed(1)}L`} />
                                    <Tooltip formatter={(v) => `₹${v.toLocaleString('en-IN')}`} />
                                    <Area type="monotone" dataKey="netWorth" stroke="#1e3a8a" fill="#1e3a8a" fillOpacity={0.1} />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                        <div className="metric-display-panel" style={{height:'400px'}}>
                            <label className="panel-label">Projected Outflow Commitments (12 Months)</label>
                            <div style={{fontSize:'12px', color:'#64748b', marginBottom:'8px'}}>Horizon: {oneYearRangeLabel}</div>
                            <ResponsiveContainer width="100%" height="82%">
                                <AreaChart data={chartData} margin={{ top: 20, right: 10, left: 10, bottom: 0 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                    <XAxis dataKey="name" fontSize={10} interval={0} />
                                    <YAxis hide />
                                    <Tooltip formatter={(v) => `₹${v.toLocaleString('en-IN')}`} />
                                    <Area type="stepAfter" dataKey="outflow" stroke="#f43f5e" fill="#f43f5e" fillOpacity={0.1} />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    <div className="notification-tray-critical" style={{borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px'}}>
                        <h4 style={{marginBottom:'15px'}}>🔔 Pending Transactions</h4>
                        <div style={{display:'flex', flexDirection:'column', gap:'10px'}}>
                            {alerts.map(a => (
                                <div key={`${a.type}-${a.id}`} style={{display:'flex', justifyContent:'space-between', alignItems:'center', background:'#f8fafc', padding:'12px', borderRadius:'8px', border:'1px solid #e2e8f0'}}>
                                    <div>
                                        <strong>{a.name}</strong> <span style={{fontSize:'12px', color:'#64748b'}}>({a.type})</span>
                                        <div style={{fontSize:'12px'}}>Due: {a.nextDueDate} &bull; Account: {a.linkedAccountName || 'None'}</div>
                                    </div>
                                    <div style={{display:'flex', alignItems:'center', gap:'15px'}}>
                                        <span style={{fontWeight:'700'}}>₹{a.amount?.toLocaleString('en-IN')}</span>
                                        <button className="btn-primary" style={{padding:'6px 12px', fontSize:'12px'}} onClick={() => recordEvent(a.id, a.type)}>Mark Paid</button>
                                    </div>
                                </div>
                            ))}
                            {alerts.length === 0 && <p style={{color:'#64748b'}}>No pending actions for the next 30 days.</p>}
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'ACCOUNTS' && (
                <div>
                    {renderSubTabs()}
                    {subTab === 'ADD' || editingItem ? (
                        <div className="form-panel">
                            <h3>{editingItem ? 'Edit Account' : 'Register Account'}</h3>
                            <form onSubmit={async (e) => {
                                e.preventDefault();
                                const form = e.target;
                                const data = Object.fromEntries(new FormData(form));
                                data.retirementAsset = form.retirementAsset?.checked || false;
                                if (editingItem) data.id = editingItem.id;
                                await saveAccount(data);
                                setSubTab('VIEW'); setEditingItem(null);
                            }}>
                                <FormField label="Account Name" tooltip="Internal label"><input type="text" name="name" defaultValue={editingItem?.name} required /></FormField>
                                <FormField label="Institution" tooltip="Bank Name"><input type="text" name="institution" defaultValue={editingItem?.institution} required /></FormField>
                                <FormField label="Account Number" tooltip="Reference No."><input type="text" name="accountNo" defaultValue={editingItem?.accountNo} /></FormField>
                                <FormField label="Registered Nominee" tooltip="Optional"><input type="text" name="nominee" defaultValue={editingItem?.nominee} /></FormField>
                                <FormField label="Current Balance" tooltip="Present valuation"><input type="number" step="0.01" name="balance" defaultValue={editingItem?.balance} required /></FormField>
                                <FormField label="Last Updated" tooltip="Date"><input type="date" name="balanceUpdatedDate" defaultValue={editingItem?.balanceUpdatedDate || todayStr} required /></FormField>
                                <FormField label="Asset Class" tooltip="Broad classification">
                                    <select name="assetClass" defaultValue={editingItem?.assetClass || 'CASH_EQUIVALENTS'}>
                                        <option value="CASH_EQUIVALENTS">Cash Equivalents</option>
                                        <option value="FIXED_INCOME">Fixed Income</option>
                                        <option value="EQUITIES">Equities</option>
                                        <option value="RETIREMENT">Retirement</option>
                                        <option value="LIABILITIES">Liabilities</option>
                                    </select>
                                </FormField>
                                <FormField label="Retirement Asset" tooltip="Include this account in retirement projections">
                                    <label style={{display:'inline-flex', alignItems:'center', gap:'8px'}}>
                                        <input type="checkbox" name="retirementAsset" defaultChecked={editingItem?.retirementAsset || false} />
                                        Consider this a retirement account
                                    </label>
                                </FormField>
                                <FormField label="Account Type" tooltip="Specific product">
                                    <select name="accountType" defaultValue={editingItem?.accountType || 'SAVINGS_ACCOUNT'}>
                                        <option value="SAVINGS_ACCOUNT">Savings Account</option>
                                        <option value="FIXED_DEPOSIT">Fixed Deposit</option>
                                        <option value="MUTUAL_FUND">Mutual Fund</option>
                                        <option value="HOME_LOAN">Home Loan</option>
                                        <option value="CREDIT_CARD">Credit Card</option>
                                        <option value="CASH_WALLET">Cash/Wallet</option>
                                    </select>
                                </FormField>
                                <button type="submit" className="btn-primary">Commit Account</button>
                            </form>
                        </div>
                    ) : (
                        <div className="data-table-panel">
                            <table className="crud-table">
                                <thead><tr><th>Name</th><th>Institution</th><th>Asset Class</th><th>Balance</th><th>Actions</th></tr></thead>
                                <tbody>
                                    {accounts.map(acc => (
                                        <tr key={acc.id}>
                                            <td>
                                            <strong>{acc.name}</strong><br/>
                                            <small>{acc.accountNo}</small>
                                            {acc.retirementAsset && <div style={{marginTop:'6px', fontSize:'11px', color:'#047857'}}>Retirement Asset</div>}
                                        </td>
                                            <td>{acc.institution}</td>
                                            <td><span className="category-badge">{acc.assetClass}</span></td>
                                            <td style={{fontWeight:'700'}}>₹{acc.balance?.toLocaleString('en-IN')}</td>
                                            <td>
                                                <button className="action-btn edit" onClick={() => setEditingItem(acc)}>Edit</button>
                                                <button className="action-btn delete" onClick={() => removeAccount(acc.id)}>Drop</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}


            {activeTab === 'INCOME' && (
                <div>
                    {renderSubTabs()}
                    {subTab === 'VIEW' && (
                        <div className="metric-display-panel" style={{marginBottom:'20px', padding:'15px', borderLeft:'5px solid #10b981'}}>
                            <label className="panel-label">Projected 12-Month Total Inflow</label>
                            <h3 style={{margin:'5px 0 0 0'}}>₹{twelveMonthInflowTotal?.toLocaleString('en-IN')} <small style={{fontSize:'12px', color:'#64748b'}}>simulation</small></h3>
                        </div>
                    )}
                    {subTab === 'ADD' || editingItem ? (
                        <div className="form-panel">
                            <h3>Register Inflow</h3>
                            <form onSubmit={async (e) => {
                                e.preventDefault();
                                const formData = new FormData(e.target);
                                const data = Object.fromEntries(formData.entries());
                                if (editingItem) data.id = editingItem.id;
                                if (data.destinationAccount) data.destinationAccount = { id: data.destinationAccount };
                                await saveIncome(data);
                                setSubTab('VIEW'); setEditingItem(null);
                            }}>
                                <FormField label="Source Name" tooltip="e.g. Salary"><input type="text" name="name" defaultValue={editingItem?.name} required /></FormField>
                                <FormField label="Institution Name" tooltip="Bank or Payer"><input type="text" name="institutionName" defaultValue={editingItem?.institutionName} /></FormField>
                                <FormField label="Amount" tooltip="Recurring amount"><input type="number" step="0.01" name="amount" defaultValue={editingItem?.amount} required /></FormField>
                                <FormField label="Frequency" tooltip="Cycle">
                                    <select name="frequency" defaultValue={editingItem?.frequency || 'MONTHLY'}>
                                        <option value="MONTHLY">Monthly</option>
                                        <option value="YEARLY">Yearly</option>
                                        <option value="ONE_TIME">One-time payout</option>
                                    </select>
                                </FormField>
                                <FormField label="Start Date" tooltip="Commencement"><input type="date" name="startDate" defaultValue={editingItem?.startDate || todayStr} required /></FormField>
                                <FormField label="Next Expected Date" tooltip="Upcoming credit"><input type="date" name="nextExpectedDate" defaultValue={editingItem?.nextExpectedDate || todayStr} required /></FormField>
                                <FormField label="Destination Account" tooltip="Account to credit">
                                    <select name="destinationAccount" defaultValue={editingItem?.destinationAccount?.id}>
                                        <option value="">None</option>
                                        {accounts.filter(a => a.assetClass !== 'LIABILITIES').map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
                                    </select>
                                </FormField>
                                <button type="submit" className="btn-primary">Register Inflow</button>
                            </form>
                        </div>
                    ) : (
                        <div className="data-table-panel">
                            <table className="crud-table">
                                <thead><tr><th>Source</th><th>Institution</th><th>Amount</th><th>Frequency</th><th>Actions</th></tr></thead>
                                <tbody>
                                    {incomes.map(inc => (
                                        <tr key={inc.id}>
                                            <td><strong>{inc.name}</strong></td>
                                            <td>{inc.institutionName}</td>
                                            <td style={{color:'#10b981'}}>₹{inc.amount?.toLocaleString('en-IN')}</td>
                                            <td>{inc.frequency}</td>
                                            <td>
                                                <button className="action-btn edit" onClick={() => setEditingItem(inc)}>Edit</button>
                                                <button className="action-btn delete" onClick={() => removeIncome(inc.id)}>Drop</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'OBLIGATIONS' && (
                <div>
                    {renderSubTabs()}
                    {subTab === 'VIEW' && (
                        <div className="metric-display-panel" style={{marginBottom:'20px', padding:'15px', borderLeft:'5px solid #ef4444'}}>
                            <label className="panel-label">Projected 12-Month Total Outflow</label>
                            <h3 style={{margin:'5px 0 0 0'}}>₹{twelveMonthOutflowTotal?.toLocaleString('en-IN')} <small style={{fontSize:'12px', color:'#64748b'}}>simulation</small></h3>
                        </div>
                    )}
                    {subTab === 'ADD' || editingItem ? (
                        <div className="form-panel">
                            <h3>Register Obligation</h3>
                            <form onSubmit={async (e) => {
                                e.preventDefault();
                                const form = e.target;
                                const formData = new FormData(form);
                                const data = Object.fromEntries(formData.entries());
                                data.retirementInstrument = form.retirementInstrument?.checked || false;
                                // Map mutual-fund specific fields into referenceNo for SIP handling
                                if ((!data.referenceNo || data.referenceNo === '') && data.sipSymbol) data.referenceNo = data.sipSymbol;
                                if ((!data.referenceNo || data.referenceNo === '') && data.folio) data.referenceNo = data.folio;
                                if (editingItem) data.id = editingItem.id;
                                if (data.linkedAccount) {
                                    data.linkedAccount = { id: data.linkedAccount };
                                } else {
                                    delete data.linkedAccount;
                                }
                                await saveObligation(data);
                                setSubTab('VIEW'); setEditingItem(null);
                            }}>
                                <FormField label="Description" tooltip="e.g. Loan Premium"><input type="text" name="instrumentName" defaultValue={editingItem?.instrumentName} required /></FormField>
                                <FormField label="Institution" tooltip="Receiver"><input type="text" name="institutionName" defaultValue={editingItem?.institutionName} /></FormField>
                                <FormField label="Reference Number" tooltip="Loan A/c or Policy No."><input type="text" name="referenceNo" defaultValue={editingItem?.referenceNo} /></FormField>
                                <FormField label="SIP Symbol / Folio" tooltip="Mutual Fund symbol or folio number (optional)"><input type="text" name="sipSymbol" placeholder="E.g. HDFC-ELSS" /></FormField>
                                <FormField label="Folio" tooltip="Mutual Fund folio number (optional)"><input type="text" name="folio" placeholder="Folio/Client ID" /></FormField>
                                <FormField label="Registered Nominee" tooltip="Optional"><input type="text" name="nominee" defaultValue={editingItem?.nominee} /></FormField>
                                <FormField label="Amount" tooltip="Installment cost"><input type="number" step="0.01" name="amount" defaultValue={editingItem?.amount} required /></FormField>
                                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                                    <FormField label="Next Due Date" tooltip="Upcoming payment"><input type="date" name="nextDueDate" defaultValue={editingItem?.nextDueDate || todayStr} required /></FormField>
                                    <FormField label="End Date" tooltip="When payments stop (Optional)"><input type="date" name="endDate" defaultValue={editingItem?.endDate} /></FormField>
                                </div>
                                <FormField label="Frequency" tooltip="Cycle">
                                    <select name="frequency" defaultValue={editingItem?.frequency || 'MONTHLY'}>
                                        <option value="MONTHLY">Monthly</option>
                                        <option value="QUARTERLY">Quarterly</option>
                                        <option value="YEARLY">Yearly</option>
                                    </select>
                                </FormField>
                                <FormField label="Category" tooltip="Classification">
                                    <select name="category" defaultValue={editingItem?.category || 'HOUSEHOLD_EXPENSE'}>
                                        <option value="HOUSEHOLD_EXPENSE">Household Expense</option>
                                        <option value="LOAN_EMI">Loan EMI</option>
                                        <option value="INVESTMENT_SIP">Investment SIP</option>
                                        <option value="TAX_PAYMENT">Tax Payment</option>
                                        <option value="SUBSCRIPTION">Subscription</option>
                                        <option value="GUARANTEED_RETURN">Guaranteed Return</option>
                                        <option value="ULIP">ULIP</option>
                                        <option value="HEALTH_INSURANCE">Health Insurance</option>
                                        <option value="LIFE_INSURANCE">Life Insurance</option>
                                        <option value="VEHICLE_INSURANCE">Vehicle Insurance</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                </FormField>
                                <FormField label="Source Account" tooltip="Account to debit">
                                    <select name="linkedAccount" defaultValue={editingItem?.linkedAccount?.id}>
                                        <option value="">None</option>
                                        {accounts.filter(a => a.assetClass !== 'LIABILITIES').map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
                                    </select>
                                </FormField>
                                <FormField label="Retirement Instrument" tooltip="Include payouts in retirement projection">
                                    <label style={{display:'inline-flex', alignItems:'center', gap:'8px'}}>
                                        <input type="checkbox" name="retirementInstrument" defaultChecked={editingItem?.retirementInstrument || false} />
                                        Consider this obligation as part of retirement planning
                                    </label>
                                </FormField>
                                <h4 style={{marginTop:'20px', borderTop:'1px solid #e2e8f0', paddingTop:'15px', color:'#1e3a8a'}}>Insurance & Maturity Details (Optional)</h4>
                                <FormField label="Death Benefit / Sum Assured" tooltip="One-time payout to family"><input type="number" step="1" name="deathBenefitAmount" defaultValue={editingItem?.deathBenefitAmount} placeholder="₹" /></FormField>
                                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                                    <FormField label="One-time Maturity Payout" tooltip="Lump sum at the end"><input type="number" step="0.01" name="lumpSumMaturityAmount" defaultValue={editingItem?.lumpSumMaturityAmount} /></FormField>
                                    <FormField label="Maturity Payout Date" tooltip="Date of lump sum"><input type="date" name="lumpSumMaturityDate" defaultValue={editingItem?.lumpSumMaturityDate} /></FormField>
                                </div>

                                <h4 style={{marginTop:'20px', borderTop:'1px solid #e2e8f0', paddingTop:'15px', color:'#1e3a8a'}}>Recurring Maturity Return / Annuity (Optional)</h4>
                                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                                    <FormField label="Recurring Return Amount" tooltip="Annuity or survival benefit"><input type="number" step="0.01" name="maturityIncomeAmount" defaultValue={editingItem?.maturityIncomeAmount} /></FormField>
                                    <FormField label="Return Start Date" tooltip="When income begins"><input type="date" name="maturityIncomeStartDate" defaultValue={editingItem?.maturityIncomeStartDate} /></FormField>
                                </div>
                                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'15px'}}>
                                    <FormField label="Return Duration (Years)" tooltip="How many years it lasts"><input type="number" name="maturityIncomeDurationYears" defaultValue={editingItem?.maturityIncomeDurationYears} /></FormField>
                                    <FormField label="Return Frequency" tooltip="Payout cycle">
                                        <select name="maturityIncomeFrequency" defaultValue={editingItem?.maturityIncomeFrequency || 'YEARLY'}>
                                            <option value="YEARLY">Yearly</option>
                                            <option value="MONTHLY">Monthly</option>
                                        </select>
                                    </FormField>
                                </div>
                                <button type="submit" className="btn-primary">Commit Architecture</button>
                            </form>
                        </div>
                    ) : (
                        <div className="data-table-panel">
                            <div style={{padding:'15px', borderBottom:'1px solid #e2e8f0', display:'flex', gap:'15px', alignItems:'center', flexDirection:'column'}}>
                                <div style={{display:'flex', gap:'8px', marginBottom:'10px', flexWrap:'wrap'}}>
                                    {['ALL','HOUSEHOLD_EXPENSE','LOAN_EMI','INVESTMENT_SIP','TAX_PAYMENT','SUBSCRIPTION','OTHER','GUARANTEED_RETURN','ULIP','HEALTH_INSURANCE','LIFE_INSURANCE','VEHICLE_INSURANCE'].map(cat => (
                                        <button key={cat} className={categoryTab === cat ? 'tab-btn active' : 'tab-btn'} onClick={() => setCategoryTab(cat)}>
                                            {cat === 'ALL' ? 'All' : cat.replace(/_/g,' ')} <span style={{opacity:0.75, marginLeft:6}}>({categorySummary[cat]?.count || 0})</span>
                                        </button>
                                    ))}
                                </div>
                                <div style={{display:'flex', gap:'12px', marginTop:8, flexWrap:'wrap', justifyContent:'center'}}>
                                    {Object.keys(categorySummary).filter(k => k !== 'ALL' && categorySummary[k].count > 0).map(k => (
                                        <div key={k} className="category-summary" style={{padding:'8px 12px', border:'1px solid #e2e8f0', borderRadius:8, minWidth:160, background:'white'}}>
                                            <div style={{fontSize:11, color:'#64748b', fontWeight:700}}>{k.replace(/_/g,' ')}</div>
                                            <div style={{fontSize:16, fontWeight:800, marginTop:4, color:'#1e3a8a'}}>₹{categorySummary[k].annualized?.toLocaleString('en-IN')}</div>
                                            <div style={{fontSize:10, color:'#94a3b8'}}>Annualized p.a.</div>
                                        </div>
                                    ))}
                                </div>
                                <div style={{display:'flex', alignItems:'center', gap:'15px', width:'100%', marginTop:15, borderTop:'1px solid #f1f5f9', paddingTop:15}}>
                                    <label style={{fontSize:'12px', fontWeight:'700'}}>Sort Table By:</label>
                                    <select value={oblSortKey} onChange={e => setOblSortKey(e.target.value)} style={{padding:'4px 8px', borderRadius:'6px', fontSize:'12px', border:'1px solid #cbd5e1'}}>
                                        <option value="instrumentName">Description</option>
                                        <option value="amount">Amount</option>
                                        <option value="category">Category</option>
                                        <option value="linkedAccount">Source Account</option>
                                    </select>
                                </div>
                            </div>
                            <table className="crud-table">
                                <thead><tr><th>Obligation</th><th>Frequency</th><th>Amount</th><th>Annualized</th><th>Category</th><th>Source</th><th>Actions</th></tr></thead>
                                <tbody>
                                    {filteredObligations.map(obl => {
                                        const amountVal = typeof obl.amount === 'number' ? obl.amount : parseFloat(obl.amount || '0');
                                        const factor = obl.frequency === 'MONTHLY' ? 12 : (obl.frequency === 'QUARTERLY' ? 4 : 1);
                                        const ann = amountVal * factor;
                                        return (
                                            <tr key={obl.id}>
                                                <td>
                                                    <strong>{obl.instrumentName}</strong><br/>
                                                    <small>{obl.referenceNo}</small>
                                                    {obl.retirementInstrument && <div style={{marginTop:'4px', fontSize:'11px', color:'#047857'}}>Retirement</div>}
                                                </td>
                                                <td>{obl.frequency}</td>
                                                <td style={{color:'#ef4444'}}>₹{amountVal.toLocaleString('en-IN')}</td>
                                                <td style={{fontWeight:'700'}}>₹{ann.toLocaleString('en-IN')}</td>
                                                <td>{obl.category}</td>
                                                <td>
                                                    {obl.linkedAccount?.name || (
                                                        <span style={{color: '#f59e0b', display: 'flex', alignItems: 'center', gap: '4px'}} title="No bank account linked. Real-time balance alerts disabled.">
                                                            ⚠️ No Source
                                                        </span>
                                                    )}
                                                </td>
                                                <td>
                                                    <button className="action-btn edit" onClick={() => setEditingItem(obl)}>Edit</button>
                                                    <button className="action-btn delete" onClick={() => removeObligation(obl.id)}>Drop</button>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'TRANSACTIONS' && (
                <div className="crud-container">
                    {renderSubTabs()}
                    {subTab === 'ADD' ? (
                        <ManualTransactionForm
                            accounts={accounts}
                            todayStr={todayStr}
                            saveManualTransaction={saveManualTransaction}
                        />
                    ) : (
                        <>
                            <div className="metric-display-panel" style={{marginBottom:'20px', display:'grid', gridTemplateColumns:'1fr 1fr', gap:'16px'}}>
                                <div style={{padding:'16px', border:'1px solid #e2e8f0', borderRadius:'12px', background:'white'}}>
                                    <div style={{fontSize:'12px', color:'#64748b', marginBottom:'8px'}}>Current Month Spend</div>
                                    <div style={{fontSize:'28px', fontWeight:'800'}}>₹{metrics.monthlyExpenseTotal?.toLocaleString('en-IN') || '0'}</div>
                                </div>
                                <div style={{padding:'16px', border:'1px solid #e2e8f0', borderRadius:'12px', background:'white'}}>
                                    <div style={{fontSize:'12px', color:'#64748b', marginBottom:'8px'}}>Top Expense Categories</div>
                                    <div style={{display:'flex', flexDirection:'column', gap:'8px'}}>
                                        {Object.entries(metrics.monthlyExpenseByCategory || {}).sort((a,b) => Number(b[1]) - Number(a[1])).slice(0,4).map(([key, value]) => (
                                            <div key={key} style={{display:'flex', justifyContent:'space-between', fontSize:'12px'}}>
                                                <span>{key.replace(/_/g, ' ')}</span>
                                                <strong>₹{Number(value).toLocaleString('en-IN')}</strong>
                                            </div>
                                        ))}
                                        {Object.keys(metrics.monthlyExpenseByCategory || {}).length === 0 && <div style={{fontSize:'12px', color:'#94a3b8'}}>No categorized spend recorded yet.</div>}
                                    </div>
                                </div>
                            </div>
                            <div className="data-table-panel">
                                <h4>🕒 Recent History</h4>
                                <div style={{display:'flex', justifyContent:'space-between', flexWrap:'wrap', gap:'16px', marginBottom:'16px'}}>
                                    <div>
                                        <div style={{fontSize:'14px', fontWeight:'700'}}>Category view</div>
                                        <div style={{fontSize:'12px', color:'#64748b'}}>
                                            {txCategoryFilter === 'ALL' ? 'Showing all transactions' : `Filtered by ${txCategoryFilter.replace(/_/g, ' ')}`}
                                        </div>
                                    </div>
                                    <div style={{display:'flex', alignItems:'center', gap:'10px', flexWrap:'wrap'}}>
                                        <label style={{fontSize:'13px', fontWeight:'600', color:'#475569'}}>Category:</label>
                                        <select value={txCategoryFilter} onChange={e => setTxCategoryFilter(e.target.value)} style={{padding:'8px 10px', borderRadius:'8px', border:'1px solid #cbd5e1'}}>
                                            <option value="ALL">All</option>
                                            <option value="UNCATEGORIZED">Uncategorized</option>
                                            <option value="FOOD">Food</option>
                                            <option value="BILLS">Bills</option>
                                            <option value="TRANSPORT">Transport</option>
                                            <option value="HEALTH">Health</option>
                                            <option value="ENTERTAINMENT">Entertainment</option>
                                            <option value="RENT">Rent</option>
                                            <option value="SHOPPING">Shopping</option>
                                            <option value="GROCERIES">Groceries</option>
                                            <option value="UTILITIES">Utilities</option>
                                            <option value="HOUSEHOLD_EXPENSE">Household Expense</option>
                                            <option value="LOAN_EMI">Loan EMI</option>
                                            <option value="INVESTMENT_SIP">Investment SIP</option>
                                            <option value="TAX_PAYMENT">Tax Payment</option>
                                            <option value="SUBSCRIPTION">Subscription</option>
                                            <option value="GUARANTEED_RETURN">Guaranteed Return</option>
                                            <option value="ULIP">ULIP</option>
                                            <option value="HEALTH_INSURANCE">Health Insurance</option>
                                            <option value="LIFE_INSURANCE">Life Insurance</option>
                                            <option value="VEHICLE_INSURANCE">Vehicle Insurance</option>
                                            <option value="OTHER">Other</option>
                                        </select>
                                    </div>
                                </div>
                                <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(180px, 1fr))', gap:'12px', marginBottom:'18px'}}>
                                    <div style={{padding:'16px', border:'1px solid #e2e8f0', borderRadius:'12px', background:'white'}}>
                                        <div style={{fontSize:'12px', color:'#64748b', marginBottom:'6px'}}>Transactions</div>
                                        <div style={{fontSize:'22px', fontWeight:'800'}}>{filteredTransactions.length}</div>
                                    </div>
                                    <div style={{padding:'16px', border:'1px solid #e2e8f0', borderRadius:'12px', background:'white'}}>
                                        <div style={{fontSize:'12px', color:'#64748b', marginBottom:'6px'}}>Total Value</div>
                                        <div style={{fontSize:'22px', fontWeight:'800'}}>₹{(transactionCategorySummary[txCategoryFilter]?.total || 0).toLocaleString('en-IN')}</div>
                                    </div>
                                </div>
                                <table className="crud-table">
                                    <thead><tr><th>Date</th><th>Description</th><th>Amount</th><th>Type</th><th>Category</th><th>Action</th></tr></thead>
                                    <tbody>
                                        {filteredTransactions.map(tx => (
                                            <tr key={tx.id}>
                                                <td>{tx.transactionDate}</td>
                                                <td>{tx.description}</td>
                                                <td style={{fontWeight:'700'}}>₹{tx.amount?.toLocaleString('en-IN')}</td>
                                                <td><span className="category-badge">{tx.type}</span></td>
                                                <td><span className="category-badge" style={{background:'#eef2ff', color:'#3730a3'}}>{tx.category || 'UNCATEGORIZED'}</span></td>
                                                <td>
                                                    <button className="action-btn delete" onClick={() => {
                                                        if (window.confirm('Rollback this transaction and reverse the balances?')) {
                                                            removeTransaction(tx.id);
                                                        }
                                                    }}>Rollback</button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    )}
                </div>
            )}

            {activeTab === 'PROJECTIONS' && (
                <div style={{display:'flex', flexDirection:'column', gap:'25px'}}>
                    <div className="workspace-panel" style={{padding:'20px', borderRadius:'12px', border:'1px solid #e2e8f0', background:'white'}}>
                        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'20px'}}>
                            <h3 style={{color:'#1e3a8a'}}>🗓️ Projection Lens (30-Year Horizon)</h3>
                            <div style={{display:'flex', alignItems:'center', gap:'15px'}}>
                                <div style={{display:'flex', alignItems:'center', gap:'8px'}}>
                                    <label style={{fontSize:'13px', fontWeight:'600'}}>From:</label>
                                    <select value={startMonth} onChange={e => setStartMonth(e.target.value)} style={{padding:'6px 10px', borderRadius:'6px', border:'1px solid #cbd5e1'}}>
                                        {forecast.map(f => <option key={f.date} value={f.date}>{f.date}</option>)}
                                    </select>
                                </div>
                                <div style={{display:'flex', alignItems:'center', gap:'8px'}}>
                                    <label style={{fontSize:'13px', fontWeight:'600'}}>To:</label>
                                    <select value={endMonth} onChange={e => setEndMonth(e.target.value)} style={{padding:'6px 10px', borderRadius:'6px', border:'1px solid #cbd5e1'}}>
                                        {forecast.map(f => <option key={f.date} value={f.date}>{f.date}</option>)}
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(200px, 1fr))', gap:'20px'}}>
                            <div className="metric-display-panel" style={{background:'#f0fdf4', border:'1px solid #d1fae5'}}>
                                <label className="panel-label">Range Expected Inflow</label>
                                <h3 className="panel-amount" style={{color:'#15803d'}}>₹{rangeMetrics.totalInflow?.toLocaleString('en-IN')}</h3>
                                <div style={{fontSize:11, color:'#64748b'}}>Total credits for selected {selectionMonths} months</div>
                            </div>
                            <div className="metric-display-panel" style={{background:'#fef2f2', border:'1px solid #fee2e2'}}>
                                <label className="panel-label">Range Expected Outflow</label>
                                <h3 className="panel-amount" style={{color:'#be123c'}}>₹{rangeMetrics.totalOutflow?.toLocaleString('en-IN')}</h3>
                                <div style={{fontSize:11, color:'#64748b'}}>Total debits for selected {selectionMonths} months</div>
                            </div>
                            <div className="metric-display-panel" style={{background: '#eff6ff', border: '1px solid #bfdbfe'}}>
                                <label className="panel-label">Cumulative Savings Capacity</label>
                                <h3 className="panel-amount" style={{color: '#1e40af'}}>
                                    ₹{(rangeMetrics.totalInflow - rangeMetrics.totalOutflow).toLocaleString('en-IN')}
                                </h3>
                                <div style={{fontSize:11, color:'#64748b'}}>Net surplus over selected period</div>
                            </div>
                        </div>
                    </div>

                    <div className="data-table-panel" style={{borderRadius:'12px'}}>
                        <h4 style={{padding:'15px', borderBottom:'1px solid #e2e8f0', background:'#f8fafc'}}>📦 Breakdown ({startMonth} to {endMonth})</h4>
                        <div style={{overflowX:'auto'}}>
                            <table className="crud-table">
                                <thead>
                                    <tr><th>Month</th><th>Inflow</th><th>Outflow</th><th>Net</th><th>Events</th></tr>
                                </thead>
                                <tbody>
                                    {rangeData.map((m, i) => {
                                        const inflow = typeof m.netInflow === 'number' ? m.netInflow : parseFloat(m.netInflow || 0);
                                        const outflow = typeof m.netOutflow === 'number' ? m.netOutflow : parseFloat(m.netOutflow || 0);
                                        const net = inflow - outflow;
                                        return (
                                            <tr key={i} style={{background: net < 0 ? '#fff1f2' : 'transparent'}}>
                                                <td><strong>{m.date}</strong></td>
                                                <td style={{color:'#15803d'}}>₹{inflow?.toLocaleString('en-IN')}</td>
                                                <td style={{color:'#be123c'}}>₹{outflow?.toLocaleString('en-IN')}</td>
                                                <td style={{fontWeight:'700', color: net >= 0 ? '#166534' : '#b91c1c'}}>₹{net.toLocaleString('en-IN')}</td>
                                                <td>{m.description}</td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'RETIREMENT' && (
                <div style={{display:'grid', gap:'24px'}}>
                    <div className="workspace-panel" style={{padding:'20px', borderRadius:'12px', border:'1px solid #e2e8f0', background:'white'}}>
                        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'20px'}}>
                            <h3 style={{color:'#1e3a8a'}}>🧭 Retirement Projection</h3>
                            <div style={{display:'flex', alignItems:'center', gap:'15px'}}>
                                <div style={{display:'flex', alignItems:'center', gap:'8px'}}>
                                    <label style={{fontSize:'13px', fontWeight:'600'}}>From:</label>
                                    <select value={startMonth} onChange={e => setStartMonth(e.target.value)} style={{padding:'6px 10px', borderRadius:'6px', border:'1px solid #cbd5e1'}}>
                                        {forecast.map(f => <option key={f.date} value={f.date}>{f.date}</option>)}
                                    </select>
                                </div>
                                <div style={{display:'flex', alignItems:'center', gap:'8px'}}>
                                    <label style={{fontSize:'13px', fontWeight:'600'}}>To:</label>
                                    <select value={endMonth} onChange={e => setEndMonth(e.target.value)} style={{padding:'6px 10px', borderRadius:'6px', border:'1px solid #cbd5e1'}}>
                                        {forecast.map(f => <option key={f.date} value={f.date}>{f.date}</option>)}
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(220px, 1fr))', gap:'20px'}}>
                            <div className="metric-display-panel" style={{background:'#f0fdf4', border:'1px solid #d1fae5'}}>
                                <label className="panel-label">Retirement Account Balance</label>
                                <h3 className="panel-amount" style={{color:'#15803d'}}>₹{retirementProjection?.retirementAccountBalance?.toLocaleString('en-IN') || 0}</h3>
                                <div style={{fontSize:11, color:'#64748b'}}>Current balances from retirement-marked accounts</div>
                            </div>
                            <div className="metric-display-panel" style={{background:'#eff6ff', border:'1px solid #bfdbfe'}}>
                                <label className="panel-label">Projected Retirement Income</label>
                                <h3 className="panel-amount" style={{color:'#1e40af'}}>₹{(retirementProjection?.projectedRecurringIncome || 0)?.toLocaleString('en-IN')}</h3>
                                <div style={{fontSize:11, color:'#64748b'}}>Recurring annuity and maturity income in range</div>
                            </div>
                            <div className="metric-display-panel" style={{background:'#fef2f2', border:'1px solid #fee2e2'}}>
                                <label className="panel-label">Projected Lump Sum</label>
                                <h3 className="panel-amount" style={{color:'#be123c'}}>₹{(retirementProjection?.projectedLumpSum || 0)?.toLocaleString('en-IN')}</h3>
                                <div style={{fontSize:11, color:'#64748b'}}>Maturity payouts expected in selected window</div>
                            </div>
                            <div className="metric-display-panel" style={{background:'#fff7ed', border:'1px solid #fed7aa'}}>
                                <label className="panel-label">Total Retirement Projection</label>
                                <h3 className="panel-amount" style={{color:'#c2410c'}}>₹{(retirementProjection?.totalProjectedRetirement || 0)?.toLocaleString('en-IN')}</h3>
                                <div style={{fontSize:11, color:'#64748b'}}>Current retirement balance plus future inflows</div>
                            </div>
                        </div>
                    </div>
                    <div className="data-table-panel" style={{borderRadius:'12px'}}>
                        <h4 style={{padding:'15px', borderBottom:'1px solid #e2e8f0', background:'#f8fafc'}}>📌 Retirement Breakdown</h4>
                        <div style={{display:'grid', gap:'20px', padding:'15px'}}>
                            <div>
                                <h5>Retirement Accounts</h5>
                                <table className="crud-table">
                                    <thead><tr><th>Name</th><th>Institution</th><th>Balance</th></tr></thead>
                                    <tbody>
                                        {retirementProjection?.retirementAccounts?.length ? retirementProjection.retirementAccounts.map((account, idx) => (
                                            <tr key={idx}>
                                                <td>{account.name}</td>
                                                <td>{account.institution}</td>
                                                <td style={{fontWeight:'700'}}>₹{Number(account.balance || 0).toLocaleString('en-IN')}</td>
                                            </tr>
                                        )) : <tr><td colSpan={3} style={{color:'#64748b', padding:'12px'}}>No retirement accounts selected.</td></tr>}
                                    </tbody>
                                </table>
                            </div>
                            <div>
                                <h5>Retirement Instruments</h5>
                                <table className="crud-table">
                                    <thead><tr><th>Obligation</th><th>Recurring Income</th><th>Lump Sum</th></tr></thead>
                                    <tbody>
                                        {retirementProjection?.retirementObligations?.length ? retirementProjection.retirementObligations.map((obl, idx) => (
                                            <tr key={idx}>
                                                <td>{obl.instrumentName}</td>
                                                <td style={{color:'#1e40af'}}>₹{Number(obl.projectedRecurringIncome || 0).toLocaleString('en-IN')}</td>
                                                <td style={{color:'#be123c'}}>₹{Number(obl.projectedLumpSum || 0).toLocaleString('en-IN')}</td>
                                            </tr>
                                        )) : <tr><td colSpan={3} style={{color:'#64748b', padding:'12px'}}>No retirement obligations defined.</td></tr>}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            )}
            {activeTab === 'DATA_MANAGEMENT' && <ImportExport />}
        </div>
    );
}

export default function App() {
    return <FinanceProvider><ConsoleDashboard /></FinanceProvider>;
}