<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  profile: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  serverErrors: { type: Object, default: () => ({}) },
  requestError: { type: String, default: '' }
})
const emit = defineEmits(['submit', 'load-example', 'load-junior-accountant-example'])
const localErrors = ref({})

const errors = computed(() => ({ ...localErrors.value, ...props.serverErrors }))

function validate() {
  const next = {}
  const { resumeText, jobText, breakMonths, hoursPerWeek } = props.profile
  if (!resumeText?.trim()) next.resumeText = 'Your accounting experience is required.'
  else if (resumeText.length < 200 || resumeText.length > 15000) next.resumeText = 'Must be between 200 and 15000 characters.'
  if (!jobText?.trim()) next.jobText = 'Target job description is required.'
  else if (jobText.length < 100 || jobText.length > 10000) next.jobText = 'Must be between 100 and 10000 characters.'
  if (breakMonths === null || breakMonths === '' || Number(breakMonths) < 0 || Number(breakMonths) > 240) next.breakMonths = 'Must be between 0 and 240.'
  if (hoursPerWeek === null || hoursPerWeek === '' || Number(hoursPerWeek) < 1 || Number(hoursPerWeek) > 40) next.hoursPerWeek = 'Must be between 1 and 40.'
  localErrors.value = next
  return Object.keys(next).length === 0
}

function submit() {
  if (validate()) emit('submit')
}
</script>

<template>
  <section class="panel" aria-labelledby="profile-title">
    <p class="step-label">INPUT</p>
    <h2 id="profile-title">Tell us about your return</h2>
    <form novalidate @submit.prevent="submit">
      <label for="resume">Your accounting experience / resume</label>
      <textarea id="resume" v-model="profile.resumeText" :aria-invalid="Boolean(errors.resumeText)" rows="9" required />
      <p v-if="errors.resumeText" class="field-error">{{ errors.resumeText }}</p>

      <label for="job">Target job description</label>
      <textarea id="job" v-model="profile.jobText" :aria-invalid="Boolean(errors.jobText)" rows="7" required />
      <p v-if="errors.jobText" class="field-error">{{ errors.jobText }}</p>

      <div class="form-grid">
        <div>
          <label for="breakMonths">Time away (months)</label>
          <input id="breakMonths" v-model.number="profile.breakMonths" type="number" min="0" max="240" required />
          <p v-if="errors.breakMonths" class="field-error">{{ errors.breakMonths }}</p>
        </div>
        <div>
          <label for="hours">Hours per week you can prepare</label>
          <input id="hours" v-model.number="profile.hoursPerWeek" type="number" min="1" max="40" required />
          <p v-if="errors.hoursPerWeek" class="field-error">{{ errors.hoursPerWeek }}</p>
        </div>
      </div>

      <label for="reason">Reason for break</label>
      <select id="reason" v-model="profile.breakReason">
        <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
        <option value="PARENTAL_LEAVE">Parental leave</option>
        <option value="CAREGIVING">Caregiving</option>
        <option value="HEALTH">Health</option>
        <option value="RELOCATION">Relocation</option>
        <option value="OTHER">Other</option>
      </select>
      <p class="helper">Optional. We only use this to tailor your career-break wording if you choose to share it.</p>

      <p v-if="requestError" class="request-error" role="alert">{{ requestError }}</p>
      <div class="actions">
        <button class="primary" type="submit" :disabled="loading">Build my re-entry map</button>
        <button class="secondary" type="button" :disabled="loading" @click="emit('load-example')">Load example</button>
        <button class="secondary" type="button" :disabled="loading" @click="emit('load-junior-accountant-example')">Load Junior Accountant example</button>
      </div>
      <p v-if="loading" class="loading" role="status">Reading your experience…</p>
    </form>
  </section>
</template>
