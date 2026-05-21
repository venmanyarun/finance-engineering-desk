import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';

const FinanceContext = createContext(null);

export function FinanceProvider({ children }) {
    const [metrics, setMetrics] = useState({ 
        totalAssets: 0, totalLiabilities: 0, netWorth: 0, 
        assetAllocation: {},
        monthlyInflow: 0, monthlyOutflow: 0, monthlySurplus: 0
    });
    const [accounts, setAccounts] = useState([]);
    const [incomes, setIncomes] = useState([]);
    const [obligations, setObligations] = useState([]);
    const [transactions, setTransactions] = useState([]);
    const [alerts, setAlerts] = useState([]);
    const [portfolios, setPortfolios] = useState([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);

    const getAuthHeaders = useCallback(() => {
        const token = localStorage.getItem('token');
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }, []);

    const fetchCoreTelemetry = useCallback(async () => {
        if (!localStorage.getItem('token')) {
            setLoading(false);
            return;
        }
        try {
            const headers = getAuthHeaders();
            const [resMetrics, resAccounts, resIncomes, resObligations, resTransactions, resAlerts, resPortfolios] = await Promise.all([
                fetch('http://localhost:8080/api/finance/dashboard-summary', { headers }).then(r => r.json()),
                fetch('http://localhost:8080/api/finance/accounts', { headers }).then(r => r.json()),
                fetch('http://localhost:8080/api/finance/income', { headers }).then(r => r.json()),
                fetch('http://localhost:8080/api/finance/obligations', { headers }).then(r => r.json()),
                fetch('http://localhost:8080/api/finance/transactions', { headers }).then(r => r.json()),
                fetch('http://localhost:8080/api/finance/active-alerts?lookaheadDays=30', { headers }).then(r => r.json()),
                fetch('http://localhost:8080/api/finance/portfolios', { headers }).then(r => r.json())
            ]);
            setMetrics(resMetrics);
            setAccounts(resAccounts);
            setIncomes(resIncomes);
            setObligations(resObligations);
            setTransactions(resTransactions);
            setAlerts(resAlerts);
            setPortfolios(resPortfolios);
        } catch (err) {
            console.error("Secure ledger sync fault", err);
        } finally {
            setLoading(false);
        }
    }, [getAuthHeaders]);

    useEffect(() => {
        const savedUser = localStorage.getItem('username');
        if (savedUser) setUser(savedUser);
        fetchCoreTelemetry();
    }, [fetchCoreTelemetry]);

    const login = async (username, password) => {
        const res = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (res.ok) {
            const data = await res.json();
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            setUser(data.username);
            await fetchCoreTelemetry();
            return true;
        }
        return false;
    };

    const register = async (username, password) => {
        const res = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        return res.ok;
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        setUser(null);
        setMetrics({ totalAssets: 0, totalLiabilities: 0, netWorth: 0, assetAllocation: {}, monthlyInflow: 0, monthlyOutflow: 0, monthlySurplus: 0 });
        setAccounts([]);
        setIncomes([]);
        setObligations([]);
        setTransactions([]);
        setAlerts([]);
    };

    const saveAccount = async (account) => {
        await fetch('http://localhost:8080/api/finance/accounts', {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify(account)
        });
        await fetchCoreTelemetry();
    };

    const removeAccount = async (id) => {
        await fetch(`http://localhost:8080/api/finance/accounts/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        await fetchCoreTelemetry();
    };

    const saveIncome = async (income) => {
        await fetch('http://localhost:8080/api/finance/income', {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify(income)
        });
        await fetchCoreTelemetry();
    };

    const removeIncome = async (id) => {
        await fetch(`http://localhost:8080/api/finance/income/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        await fetchCoreTelemetry();
    };

    const saveObligation = async (obligation) => {
        await fetch('http://localhost:8080/api/finance/obligations', {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify(obligation)
        });
        await fetchCoreTelemetry();
    };

    const removeObligation = async (id) => {
        await fetch(`http://localhost:8080/api/finance/obligations/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        await fetchCoreTelemetry();
    };

    const recordEvent = async (id, type) => {
        await fetch('http://localhost:8080/api/finance/transactions/record-event', {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, type })
        });
        await fetchCoreTelemetry();
    };

    const saveManualTransaction = async (tx) => {
        await fetch('http://localhost:8080/api/finance/transactions/manual', {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify(tx)
        });
        await fetchCoreTelemetry();
    };

    const savePortfolio = async (p) => {
        const res = await fetch('http://localhost:8080/api/finance/portfolios', {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify(p)
        });
        if (!res.ok) {
            const txt = await res.text();
            throw new Error(txt || 'Failed to create portfolio');
        }
        await fetchCoreTelemetry();
    };

    const deletePortfolio = async (id) => {
        const res = await fetch(`http://localhost:8080/api/finance/portfolios/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!res.ok) {
            const txt = await res.text();
            throw new Error(txt || 'Failed to delete portfolio');
        }
        await fetchCoreTelemetry();
    };

    const saveHolding = async (portfolioId, h) => {
        await fetch(`http://localhost:8080/api/finance/portfolios/${portfolioId}/holdings`, {
            method: 'POST',
            headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify(h)
        });
        await fetchCoreTelemetry();
    };

    const fetchHoldings = async (portfolioId) => {
        const res = await fetch(`http://localhost:8080/api/finance/portfolios/${portfolioId}/holdings`, { headers: getAuthHeaders() });
        if (res.ok) return await res.json();
        return [];
    };

    const getLatestPrice = async (symbol, exchange = '') => {
        const res = await fetch(`http://localhost:8080/api/finance/market-prices?symbol=${encodeURIComponent(symbol)}&exchange=${encodeURIComponent(exchange)}`, { headers: getAuthHeaders() });
        if (res.ok) return await res.json();
        return null;
    };

    const uploadMarketPricesFile = (file, onProgress) => {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open('POST', 'http://localhost:8080/api/finance/market-prices/import-file');
            const token = localStorage.getItem('token');
            if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);
            xhr.upload.onprogress = (e) => { if (onProgress && e.lengthComputable) onProgress(Math.round((e.loaded / e.total) * 100)); };
            xhr.onload = () => { if (xhr.status >= 200 && xhr.status < 300) resolve(); else reject(xhr.responseText || xhr.statusText); };
            xhr.onerror = () => reject('Network error');
            const fd = new FormData();
            fd.append('file', file);
            xhr.send(fd);
        });
    };

    const removeHolding = async (id) => {
        const res = await fetch(`http://localhost:8080/api/finance/holdings/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!res.ok) {
            const txt = await res.text();
            throw new Error(txt || 'Failed to delete holding');
        }
        await fetchCoreTelemetry();
    };

    return (
        <FinanceContext.Provider value={{
            metrics, accounts, incomes, obligations, transactions, alerts, loading, user, portfolios,
            login, register, logout, saveAccount, removeAccount, saveIncome, removeIncome,
            saveObligation, removeObligation, recordEvent, saveManualTransaction,
            savePortfolio, saveHolding, removeHolding, deletePortfolio,
            fetchHoldings, getLatestPrice, uploadMarketPricesFile,
            fetchCoreTelemetry, getAuthHeaders
        }}>
            {children}
        </FinanceContext.Provider>
    );
}

export const useFinance = () => useContext(FinanceContext);