<template id="activity-profile">
  <app-layout>
    <div v-if="noActivityFound">
      <p>Sorry, we could not find this activity.</p>
      <p>View <a href="/activities">all activities</a>.</p>
    </div>
    <div class="card bg-light mb-3" v-if="activity">
      <div class="card-header">Activity Details</div>
      <div class="card-body">
        <form>
          <label class="col-form-label">Activity ID:</label>
          <input class="form-control" v-model="activity.id" type="number" readonly/><br>
          <label class="col-form-label">Description:</label>
          <input class="form-control" v-model="activity.description" type="text" readonly/><br>
          <label class="col-form-label">Duration (mins):</label>
          <input class="form-control" v-model="activity.duration" type="number" readonly/><br>
          <label class="col-form-label">Calories:</label>
          <input class="form-control" v-model="activity.calories" type="number" readonly/><br>
          <label class="col-form-label">Started:</label>
          <input class="form-control" :value="new Date(activity.started).toLocaleString()" type="text" readonly/><br>
        </form>
      </div>
      <div class="card-footer">
        <span v-if="user">
          Done by <a :href="`/users/${user.id}`">{{ user.name }}</a>
        </span>
        <span v-else>
          Done by user {{ activity.userId }}
        </span>
      </div>
    </div>
  </app-layout>
</template>

<script>
app.component("activity-profile", {
  template: "#activity-profile",
  data: () => ({
    activity: null,
    user: null,
    noActivityFound: false
  }),
  created() {
    const activityId = this.$javalin.pathParams["activity-id"];
    axios.get(`/api/activities/${activityId}`)
        .then(res => {
          this.activity = res.data;
          return axios.get(`/api/users/${this.activity.userId}`);
        })
        .then(res => this.user = res.data)
        .catch(() => {
          if (!this.activity) {
            this.noActivityFound = true;
          }
        });
  }
});
</script>