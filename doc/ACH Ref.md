# TRE (Transaction Risk Exposure) Documentation Wiki

![image.png](image.png)

> The TRE (Transaction Risk Exposure) team acts as the automated "safety net" for the bank's electronic money transfers, **specifically serving wholesale customers**.
> 
> Before a business can send out massive batches of payments (like payroll) or pull money from customers, the TRE system instantly checks if that business has enough actual cash or approved credit to cover the total amount. **Note: TRE does not execute financial settlements or deal with direct money movement; it solely evaluates risk to prevent financial loss.** Essentially, they function as a high-speed financial bouncer—stopping risky transactions in their tracks to ensure the bank doesn't accidentally lose millions of dollars.

---

## 1. System Mission & Business Context

**The Core Mission**
The TRE (Transaction Risk Exposure) application is the critical risk gateway for wholesale transaction processing. Its primary mandate is to evaluate, approve, or suspend batch files based on real-time and multi-day exposure limits before they are released to the Federal Reserve. TRE assesses two primary types of transactions: **ACH** and **ICING** (checks).

**Business Impact & Scale**
* **Customer Base:** Manages risk exposure for approximately **45,000 wholesale customers**.
* **Financial Protection:** Prevents massive financial loss by ensuring customers do not transmit funds exceeding their approved credit thresholds or current account balances.
* **Automation Necessity:**
    * Processing at this scale requires highly automated, low-latency decisioning.
    * While standard transactions are evaluated via matrix logic and MQ queues instantaneously, exceptions trigger automated "Risk Alerts" for manual intervention by Responsible Credit Officers and ACH Operations.

---

## 2. Architecture Analysis Template

### A. Core Data Flow & Routing
The foundational decision pipeline operates as follows:
`ACH` ➔ `ACH_MESSAGE_LOG` ➔ `TRE MATRIX` ➔ `RISK ALERT` (if flagged) ➔ `BANKER APPROVE/DECLINE` ➔ `ACH_MESSAGE_LOG` ➔ `ACH`

1. **Ingestion & Logging:** Customers submit Direct Deposit/Payment files to **WF ACH**. WF ACH sends an XML message request via MQ (`NA.TRE.BATCH.REQUEST`) to TRE. All raw messages from the MQ batch are inserted into the `ach_message_log` table (or `icing_message_log` for checks).
    * *XML Payload details:* Contains `<Header>`, `transactionId` (used for activity and risk alert tracking), `fileId`, `accountNumber`, `accountType` (e.g., DDA), `onUsInd`, `prepaidInd`, and `tranCode` (EVAL, EVALS [same day], EVALL [late day], REEVAL, APPR).
    * *RiskItem Tags:* The XML includes specific item data such as `secCode` (e.g., PPD), credit/debit amounts, current limits, and existing exposures.
2. **Evaluation:** TRE queries limits, current multi-day exposure, and customer Credit Risk Ratings.
    * *Ratings Fallback Logic:* Credit risk ratings (BQR, CQR, AQR, PD) are loaded in an order of priority from multiple sources. If the latest info is unavailable, the previous information carries forward as a fallback.
    * *If Prepaid (`prepaidInd: Y`):* TRE makes an HTTP call to **TMS** (which fetches balance info from **RCS**) to check balances. **ILMS** handles the memo posting and maintains the real-time balance of the bank.
3. **Decision:**
    * **Approve:** TRE replies via MQ (`NA.TRE.BATCH.REPLY`) with a `tranCode` of `APPR` and logs the exposure. The file moves to the **ACH Warehouse** for Federal Reserve routing.
    * **Pend/Decline:** TRE returns a `PEND`, `DECL`, or `HOLD` status, creates a Risk Alert (status: A, PA, CA, AO, HD), and triggers event messages via MQ (`TRE.SMU.NOTIFYS`). If the `SENT_EXT_EMAIL` flag is triggered, an external email is sent via SMTP to the customer/banker/credit officers.

### B. Component Breakdown
* **TRE Engine:** The core decisioning matrix that calculates limits vs. requested batch totals.
* **TRE Matrix:** The rules engine applied when a transaction goes over a standard limit.
* **TMS (Treasury Management System):** Interfaces with TRE for real-time balance checks and memo-debiting (specifically for Prepaid customers).
* **ILMS:** Responsible for maintaining the real-time balance of the bank and handling memo posting.

### C. Integrations & Data Sources
* **Accounts:** Account data is loaded directly from **Hogan**.
* **Underwriting/Credit:** Receives daily NDM files from **AFS** (Customer Grade) and underwriting systems (**CATools, Credit View, Athena Blast**). AQR ratings are updated once a day via TMA API calls.
* **Reference Data:** Integrated with **Hogan CIS** (Legal Name, Tax ID) and **MCV** (Tax ID grouping, AU/LG groupings).
* **RBAC:** Roles and permissions are managed via the `portfolio_manager` table.
* **Reporting:** Data flows into **Oracle PNL2**, then to **SQLServer** for **SSRS** (SQL Server Reporting Services) reporting.

---

## 3. ACH Risk Domain Guide

ACH is not a real-time money movement network; it relies on **settlement dates** and **multi-day processing windows**.

### Terminology Table

| **Term** | **Definition** |
| :--- | :--- |
| **ACH Exposure** | The total dollar amount of unsettled, in-flight ACH transactions a customer has pending at any given moment. |
| **ACH Limits** | The maximum allowable exposure for a specific transaction type. Limits are segregated by type (ACHC, ACHP, ACHD). |
| **ACHC (Credit Qualified)** | Transactions where the customer is *sending* money (e.g., Payroll). Risk: The bank sends the money to the Fed, but the customer's account doesn't have the funds to cover it on settlement day. |
| **ACHP (Prepaid Credit)** | Like ACHC, but the customer must have the cash in their account *right now*. TRE checks the balance via TMS and memo-debits the account immediately via ILMS. |
| **ACHD (Debit Qualified)** | Transactions where the customer is *pulling* money from external accounts. Risk: The pulled funds are returned (e.g., insufficient funds at the external bank) *after* the customer has already spent the credited money. |
| **Settlement Date** | The future date when funds actually move between the banks at the Federal Reserve. |

### The Logic of Limits: Multi-Day Exposure Calculation

Exposure **stacks across days**. Limits are not evaluated on a per-file basis, but on a rolling timeline.

**Example Calculation:** If a customer submits three $25,000 files with overlapping settlement windows, their exposure aggregates on the overlapping days.

| | **Day 1** | **Day 2** | **Day 3** | **Day 4** | **Day 5** |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **File #1** | $25,000 | $25,000 | $25,000 | | |
| **File #2** | | $25,000 | $25,000 | $25,000 | |
| **File #3** | | | $25,000 | $25,000 | $25,000 |
| **Total Exposure** | **$25,000** | **$50,000** | **$75,000** | **$50,000** | **$25,000** |

**TRE ensures that the *Total Exposure* on any given day never exceeds the customer's global ACH limit.**

---

## 4. Operational Workflows & Alert Mechanics

### Limit Setup & Matrix Administration
Limits and matrix thresholds are enforced by TRE based on Line of Business (LOB) assignments (e.g., Orbit LOB). 

1. **Review:** Credit Officers use LOB-specific tools (CATools, Credit View, Athena Blast) to underwrite the customer.
2. **Approval:** Limits are approved in **TMSAC** by Credit Officers.
3. **Ingestion:** Approved limits are currently entered manually into TRE by the TRE Support Team.

Administrators define risk parameters in the **Matrix Administration Screen** using:
1. **BDG Ranges:** (BDG >= Range Start to BDG <= Range End).
2. **Max % of Limit.**
3. **Max $ Exp Over Limit.**

### Risk Alert Resolution
When a file violates the LOB matrix rules (e.g., over limit, insufficient funds for ACHP), it enters a **PEND** status and generates a row in the `risk_alert` table.

1. **Alert Creation:** The `risk_alert` entry logs critical decision info including `APPL_SEQ_NO`, `MARKET_SEGMENT` (matrix info), `OVER_AMOUNT`, `BAL_EXP_CUST` (balance exposure), `SEC` code, and `TMS_RETRY_STATUS` (for prepaid).
2. **Notification:** Bankers receive an MQ/SMTP notification (`TRE.EMAIL.REQUEST`), tracked by the `SENT_EXT_EMAIL` flag.
3. **Review:** Credit Officers review the alert within the TRE interface (Detailed Page Screen or Exposure Screen, which provides account-level data like Tax ID, BQR, PD, RM/Officer).
4. **Action:** If approved (`DECISION_DATETIME` is logged), ACH Operations formally Approves/Declines the file in TRE, releasing the MQ reply back to ACH.

---

## 5. Notion "Wiki" Components

### Glossary of Technical Acronyms

* **AQR / BQR / CQR:** Types of Credit Risk Ratings. BQR (Borrower Downgrade Rating) is synonymous with **BDG**.
* **AU / LG:** Accounting Units / Line Groups.
* **ICING:** Internal term for check processing transactions.
* **ILMS:** System that maintains the real-time balance of the bank and handles memo posting.
* **MQ (Message Queue):** IBM MQ used for asynchronous, reliable communication between TRE and ACH.
* **NDM (Network Data Mover):** A file transfer protocol used to ingest daily batch files.
* **PD:** Probability of Default (a Credit Risk Rating).
* **RCS:** System that TMS fetches balance information from.
* **SSRS (SQL Server Reporting Services):** The BI tool used to visualize exposure and risk history.
* **TMS (Treasury Management System):** Used for real-time account interactions.

### Critical Domain Notes

> 💡 **Critical Logic: Credit vs. Debit Exposure Logging**
> Notice the difference in *when* exposure is logged based on transaction type:
> * **Credits (ACHC):** Exposure is logged *prior to and up to* the settlement date (e.g., Sent 5/29, Settled 6/3. Exposure = 5/30, 5/31, 6/3). This is because the risk ends once the settlement is successfully funded.
> * **Debits (ACHD):** Exposure is logged *on and after* the settlement date (e.g., Sent 5/29, Settled 6/3. Exposure = 6/3, 6/4, 6/5). This accounts for the **Return Window**—the days after settlement where the external bank might reject and return the debit.

> ⚠️ **Common Pitfall: The Prepaid Fallacy**
> Do not assume Prepaid (ACHP) files bypass exposure limits just because they are funded upfront. TRE still tracks their exposure limit AND actively forces a balance check/memo-debit via TMS. If the TMS check fails (Insufficient Funds), an immediate Event Message is dispatched to the customer.