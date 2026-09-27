<script setup>
import { reactive, ref } from 'vue'
import ReturnProfile from './components/ReturnProfile.vue'
import ReentryMap from './components/ReentryMap.vue'
import ComebackPlan from './components/ComebackPlan.vue'

const priyaResume = `Priya Sharma — Senior Accountant, CPA

Harbourline Services, Vancouver
Accountant / Senior Accountant
June 2018 – August 2024

- Prepared monthly journal entries and supported month-end close across three business units.
- Completed bank, balance-sheet, and general-ledger account reconciliations.
- Prepared monthly financial statements and variance-analysis reports for management.
- Used Oracle ERP and Excel for accounting reports and account analysis.
- Supported annual external audits by preparing schedules and responding to auditor requests.
- Worked with department managers to investigate expense and budget variances.

Education:
Bachelor of Commerce — Accounting

Designation:
CPA (Chartered Professional Accountant), 2021`

const priyaJob = `Senior Accountant

Responsibilities and requirements:

- Prepare journal entries and support month-end close.
- Complete balance-sheet and bank reconciliations.
- Prepare financial statements and variance reports.
- Strong Microsoft Excel skills.
- Experience with NetSuite is preferred.
- Experience with Power BI is an asset.
- Support external audit requests.
- CPA designation preferred.`

const mayaResume = `Maya Chen — Accounting Assistant

North Shore Distribution Ltd., Vancouver
Accounting Assistant
September 2019 – August 2023

- Processed accounts-payable invoices and prepared weekly payment runs.
- Prepared customer invoices and followed up on outstanding accounts receivable.
- Completed monthly bank and corporate credit-card reconciliations.
- Posted recurring journal entries and supported the month-end close process.
- Maintained accounting schedules and reports using Excel and QuickBooks Online.
- Prepared invoices, reconciliations, and supporting schedules for the annual audit.

Education:
Diploma in Accounting`

const mayaJob = `Junior Accountant - Platinum Properties Group
Vancouver, BC | Full-time

Position: The junior accountant will work closely with the project management and property management team to fulfill day-to-day as well as recurring needs. The junior accountant is responsible for various accounts payable, accounts receivable and reconciliation tasks. Prior experience is an asset but not required. We will provide full training for any assigned tasks.

Responsibilities:
- Process full cycle Accounts Payable and Accounts Receivable
- Direct liaison with Project Managers and Site Managers
- Assisting finance team with all year end requirements and other financial tasks as required
- Reconcile General Ledger
- Assist with all year end working papers and relevant schedules
- Prepare timely and accurate bank and credit card reconciliations
- Process expense reports and petty cash reimbursements
- Assist finance team with all year-end administrative and ad hoc reporting on an as needed basis

Competencies:
- Meticulous and has exceptional attention to detail
- Excellent written and verbal interpersonal communication skills
- Must have a positive "can do" attitude and willingness to go above and beyond
- Proficiency in Microsoft Office, emphasis on Excel
- Excellent organizational and time management skills, including ability to prioritize work
- Superb multi-tasker and able to work well under pressure

Qualifications:
- Must have a certificate or diploma in accounting or finance
- Minimum 1 year of relevant work experience
- Must have experience with computerized accounting systems
- Experience with property management software is an asset but not required

Salary Range: $45,000 – $55,000

Please apply through Indeed. We thank all applicants for their interest in Platinum Properties Group. But only those selected for further consideration will be contacted. Please no follow-up phone calls.

Job Type: Full-time

Pay: $45,000.00-$55,000.00 per year

Ability to commute/relocate:
- Vancouver, BC: reliably commute or plan to relocate before starting work (required)

Application question(s):
- What makes you a good fit for this position?
- How would you describe your credit?

Experience:
- Accounting: 1 year (preferred)

Work Location: In person`

const blankProfile = () => ({ resumeText: '', jobText: '', breakMonths: null, breakReason: 'PREFER_NOT_TO_SAY', hoursPerWeek: null })
const step = ref('INPUT')
const profile = reactive(blankProfile())
const result = ref(null)
const cachedDemo = ref(null)
const loading = ref(false)
const serverErrors = ref({})
const requestError = ref('')

function applyPriyaProfile() {
  Object.assign(profile, {
    resumeText: priyaResume,
    jobText: priyaJob,
    breakMonths: 24,
    breakReason: 'PARENTAL_LEAVE',
    hoursPerWeek: 5
  })
}

function applyMayaProfile() {
  Object.assign(profile, {
    resumeText: mayaResume,
    jobText: mayaJob,
    breakMonths: 24,
    breakReason: 'PARENTAL_LEAVE',
    hoursPerWeek: 4
  })
}

async function loadFixture(applyProfile, endpoint) {
  loading.value = true
  requestError.value = ''
  serverErrors.value = {}
  cachedDemo.value = null
  result.value = null
  applyProfile()
  try {
    const response = await fetch(endpoint)
    if (!response.ok) throw new Error('Demo fixture unavailable')
    cachedDemo.value = await response.json()
  } catch {
    requestError.value = 'We could not load the example right now. Please try again.'
  } finally {
    loading.value = false
  }
}

function loadExample() {
  return loadFixture(applyPriyaProfile, '/api/reentry/demo')
}

function loadJuniorAccountantExample() {
  return loadFixture(applyMayaProfile, '/api/reentry/examples/maya-junior-accountant')
}

async function buildMap() {
  loading.value = true
  requestError.value = ''
  serverErrors.value = {}
  try {
    if (!cachedDemo.value) {
      const response = await fetch('/api/reentry/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(profile)
      })
      const body = await response.json()
      if (!response.ok) {
        serverErrors.value = body.fields || {}
        requestError.value = body.error === 'INVALID_INPUT' ? 'Please correct the highlighted fields.' : 'We could not complete the analysis right now. You can try again or load the example.'
        return
      }
      result.value = body
    } else {
      result.value = cachedDemo.value
    }
    step.value = 'MAP'
  } catch {
    requestError.value = 'We could not complete the analysis right now. You can try again or load the example.'
  } finally {
    loading.value = false
  }
}

function startOver() {
  Object.assign(profile, blankProfile())
  result.value = null
  cachedDemo.value = null
  serverErrors.value = {}
  requestError.value = ''
  step.value = 'INPUT'
}
</script>

<template>
  <main class="page-shell">
    <header class="site-header">
      <p class="eyebrow">ReLaunch</p>
      <h1>Return to accounting without starting over.</h1>
      <p>Your experience still counts. See what is ready, what needs refreshing, and what you need to learn.</p>
    </header>

    <ReturnProfile
      v-if="step === 'INPUT'"
      :profile="profile"
      :loading="loading"
      :server-errors="serverErrors"
      :request-error="requestError"
      @submit="buildMap"
      @load-example="loadExample"
      @load-junior-accountant-example="loadJuniorAccountantExample"
    />
    <ReentryMap v-else-if="step === 'MAP'" :result="result" @show-plan="step = 'PLAN'" />
    <ComebackPlan v-else :result="result" :hours-per-week="profile.hoursPerWeek" @start-over="startOver" />
  </main>
</template>
