import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import App from './App.vue'

const fixture = {
  skills: [
    ...['Journal entries', 'Month-end close', 'Account reconciliations', 'Financial statements', 'Variance analysis', 'Audit support'].map(name => ({ name, status: 'READY', resumeQuote: 'Resume evidence.', reason: 'Supported by your experience.' })),
    { name: 'Excel', status: 'REFRESH', resumeQuote: 'Used Excel.', reason: 'Worth refreshing.' },
    { name: 'NetSuite', status: 'LEARN', resumeQuote: null, reason: 'Not shown in your resume.' },
    { name: 'Power BI', status: 'LEARN', resumeQuote: null, reason: 'Not shown in your resume.' }
  ],
  credentials: [{ name: 'CPA', resumeQuote: 'CPA.' }],
  plan: [{ week: 1, totalMinutes: 60, blocks: [{ minutes: 30, skill: 'Excel', task: 'Practice a sample report.' }, { minutes: 30, skill: 'Resume update', task: 'Update your resume with skills you refreshed and can now confidently discuss.' }] }],
  continueWith: [],
  interview: { breakStory: 'I took parental leave. I built accounting experience before my break. I am ready to return to an accounting team.' }
}

const mayaFixture = {
  skills: [
    ...['Accounts payable processing', 'Accounts receivable processing', 'Bank and credit-card reconciliations', 'Year-end working papers and schedules'].map(name => ({ name, status: 'READY', resumeQuote: 'Resume evidence.', reason: 'Supported by your experience.' })),
    { name: 'Excel', status: 'REFRESH', resumeQuote: 'Excel evidence.', reason: 'Worth refreshing.' },
    { name: 'Computerized accounting systems / QuickBooks Online', status: 'REFRESH', resumeQuote: 'QuickBooks Online evidence.', reason: 'Worth refreshing.' },
    ...['General ledger reconciliation', 'Expense reports and petty-cash reimbursements', 'Property-management software', 'Project and site manager liaison'].map(name => ({ name, status: 'LEARN', resumeQuote: null, reason: 'Not shown in your resume.' }))
  ],
  credentials: [],
  plan: [
    { week: 1, totalMinutes: 150, blocks: [{ minutes: 60, skill: 'Excel', task: 'Rebuild an Excel reconciliation template using sample property accounting data.' }, { minutes: 60, skill: 'General ledger reconciliation', task: 'Reconcile one sample general-ledger account and document outstanding items.' }, { minutes: 30, skill: 'Expense reports and petty-cash reimbursements', task: 'Review and process sample expense reports and petty-cash reimbursements.' }] },
    { week: 2, totalMinutes: 120, blocks: [{ minutes: 45, skill: 'Computerized accounting systems / QuickBooks Online', task: 'Refresh a sample AP/AR workflow in QuickBooks Online using non-production data.' }, { minutes: 45, skill: 'Property-management software', task: 'Review a current property-management accounting interface and identify its invoice and reconciliation workflows.' }, { minutes: 30, skill: 'Project and site manager liaison', task: 'Draft a concise email to a project manager about an invoice discrepancy.' }] },
    { week: 3, totalMinutes: 45, blocks: [{ minutes: 45, skill: 'Resume update', task: 'Update your resume with skills you refreshed and can now confidently discuss.' }] }
  ],
  continueWith: [],
  interview: { breakStory: "I took parental leave after building hands-on experience in accounts payable, accounts receivable, and account reconciliations. I'm preparing to refresh Excel and QuickBooks Online while building familiarity with general-ledger reconciliation and property-management software. I'm ready to return to a Junior Accountant role and support project and property management teams." }
}

function buttonWithText(wrapper, text) {
  return wrapper.findAll('button').find(button => button.text() === text)
}

describe('Phase 1 fixture flow', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn().mockImplementation(async url => ({
      ok: true,
      json: async () => url === '/api/reentry/examples/maya-junior-accountant' ? mayaFixture : fixture
    })))
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: { writeText: vi.fn().mockResolvedValue(undefined) }
    })
  })

  it('shows inline validation errors for an empty profile', async () => {
    const wrapper = mount(App)
    await wrapper.find('form').trigger('submit')
    expect(wrapper.text()).toContain('Your accounting experience is required.')
    expect(wrapper.text()).toContain('Target job description is required.')
    expect(wrapper.text()).toContain('Must be between 0 and 240.')
  })

  it('completes Load Example to Map to Plan to Start over', async () => {
    const wrapper = mount(App)
    await buttonWithText(wrapper, 'Load example').trigger('click')
    await flushPromises()
    expect(fetch).toHaveBeenCalledWith('/api/reentry/demo')
    expect(wrapper.text()).toContain('Tell us about your return')

    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(wrapper.text()).toContain("You don't need to relearn everything.")
    expect(wrapper.text()).toContain('7 of 9 skills this role needs are already part of your experience.')

    await buttonWithText(wrapper, 'See my comeback plan').trigger('click')
    expect(wrapper.text()).toContain('Your Comeback Plan')

    await buttonWithText(wrapper, 'Copy').trigger('click')
    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(fixture.interview.breakStory)

    await buttonWithText(wrapper, 'Start over').trigger('click')
    expect(wrapper.text()).toContain('Tell us about your return')
    expect(wrapper.get('#resume').element.value).toBe('')
    expect(wrapper.get('#reason').element.value).toBe('PREFER_NOT_TO_SAY')

    fetch.mockClear()
    await wrapper.get('#resume').setValue('x'.repeat(200))
    await wrapper.get('#job').setValue('y'.repeat(100))
    await wrapper.get('#breakMonths').setValue('6')
    await wrapper.get('#hours').setValue('2')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(fetch).toHaveBeenCalledWith('/api/reentry/analyze', expect.objectContaining({ method: 'POST' }))
  })

  it('completes the Junior Accountant example through Map, Plan, and Start over', async () => {
    const wrapper = mount(App)
    await buttonWithText(wrapper, 'Load Junior Accountant example').trigger('click')
    await flushPromises()

    expect(fetch).toHaveBeenCalledWith('/api/reentry/examples/maya-junior-accountant')
    expect(wrapper.text()).toContain('Tell us about your return')
    expect(wrapper.get('#resume').element.value).toContain('Maya Chen — Accounting Assistant')
    expect(wrapper.get('#job').element.value).toContain('Junior Accountant - Platinum Properties Group')
    expect(wrapper.get('#job').element.value).toContain('Ability to commute/relocate:')
    expect(wrapper.get('#breakMonths').element.value).toBe('24')
    expect(wrapper.get('#hours').element.value).toBe('4')
    expect(wrapper.get('#reason').element.value).toBe('PARENTAL_LEAVE')

    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(wrapper.text()).toContain('6 of 10 skills this role needs are already part of your experience.')
    expect(wrapper.text()).not.toContain('Before you apply: check your credentials')
    expect(wrapper.text()).not.toContain('CPA')

    await buttonWithText(wrapper, 'See my comeback plan').trigger('click')
    expect(wrapper.text()).toContain('Your Comeback Plan')
    expect(wrapper.text()).toContain('of 4h available')
    expect(wrapper.text()).toContain('Week 3')

    await buttonWithText(wrapper, 'Start over').trigger('click')
    expect(wrapper.text()).toContain('Tell us about your return')
    expect(wrapper.get('#resume').element.value).toBe('')
    expect(wrapper.get('#reason').element.value).toBe('PREFER_NOT_TO_SAY')
  })
})
