<script setup>
const props = defineProps({ result: { type: Object, required: true }, hoursPerWeek: { type: Number, required: true } })
defineEmits(['start-over'])

function duration(minutes) {
  const hours = Math.floor(minutes / 60)
  const remaining = minutes % 60
  return hours ? `${hours}h${remaining ? ` ${remaining}m` : ''}` : `${remaining}m`
}

async function copyStory() {
  if (navigator.clipboard) await navigator.clipboard.writeText(props.result.interview.breakStory)
}
</script>

<template>
  <section class="panel" aria-labelledby="plan-title">
    <p class="step-label">PLAN</p>
    <h2 id="plan-title">Your Comeback Plan</h2>
    <article v-for="week in result.plan" :key="week.week" class="week-card">
      <h3>Week {{ week.week }}</h3>
      <p class="week-total"><strong>{{ duration(week.totalMinutes) }} of {{ duration(hoursPerWeek * 60) }} available</strong></p>
      <ul>
        <li v-for="block in week.blocks" :key="`${week.week}-${block.skill}-${block.task}`"><strong>{{ block.minutes }} min — {{ block.skill }}</strong><br />{{ block.task }}</li>
      </ul>
    </article>
    <p v-if="result.continueWith.length" class="continue"><strong>Continue with:</strong> {{ result.continueWith.join(', ') }}</p>
    <section class="story-card">
      <h3>Talking about your break</h3>
      <p>{{ result.interview.breakStory }}</p>
      <button class="secondary" type="button" @click="copyStory">Copy</button>
    </section>
    <button class="primary" type="button" @click="$emit('start-over')">Start over</button>
  </section>
</template>
