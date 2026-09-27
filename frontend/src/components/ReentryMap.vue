<script setup>
import { computed } from 'vue'

const props = defineProps({ result: { type: Object, required: true } })
defineEmits(['show-plan'])
const groups = computed(() => ['READY', 'REFRESH', 'LEARN'].map(status => ({
  status,
  skills: props.result.skills.filter(skill => skill.status === status)
})))
const alreadyHave = computed(() => props.result.skills.filter(skill => skill.status !== 'LEARN').length)
</script>

<template>
  <section class="panel" aria-labelledby="map-title">
    <p class="step-label">MAP</p>
    <h2 id="map-title">You don't need to relearn everything.</h2>
    <p class="summary"><strong>{{ alreadyHave }} of {{ result.skills.length }} skills this role needs are already part of your experience.</strong></p>
    <div class="skill-columns">
      <section v-for="group in groups" :key="group.status" class="skill-group" :class="group.status.toLowerCase()">
        <h3>{{ group.status }}</h3>
        <p>{{ group.status === 'READY' ? 'Skills supported by your previous accounting experience that remain directly relevant.' : group.status === 'REFRESH' ? 'Skills you have used before but should revisit.' : 'Skills required by the target job where your resume contains no evidence of previous experience.' }}</p>
        <article v-for="skill in group.skills" :key="skill.name" class="skill-card">
          <h4>{{ skill.name }}</h4>
          <span class="status">{{ skill.status }}</span>
          <p><strong>Why:</strong> {{ skill.reason }}</p>
          <p v-if="skill.resumeQuote" class="evidence">From your experience: “{{ skill.resumeQuote }}”</p>
        </article>
      </section>
    </div>
    <section v-if="result.credentials.length" class="credential-card">
      <h3>Before you apply: check your credentials</h3>
      <p v-for="credential in result.credentials" :key="credential.name"><strong>{{ credential.name }}</strong> — Confirm your current status and any continuing-education or renewal requirements with the issuing body. Requirements may change while you are away.</p>
    </section>
    <button class="primary" type="button" @click="$emit('show-plan')">See my comeback plan</button>
  </section>
</template>
