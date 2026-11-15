<template id="activity-overview">
  <app-layout>
    <h2>All Activities</h2>

    <div v-if="activities.length === 0" class="alert alert-info mt-3">
      No activities found.
    </div>

    <table v-else class="table table-striped table-hover mt-3">
      <thead>
        <tr>
          <th>ID</th>
          <th>Description</th>
          <th>Duration (mins)</th>
          <th>Calories</th>
          <th>Started</th>
          <th>User</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="activity in activities" :key="activity.id">
          <td>{{ activity.id }}</td>
          <td><a :href="`/activities/${activity.id}`">{{ activity.description }}</a></td>
          <td>{{ activity.duration }}</td>
          <td>{{ activity.calories }}</td>
          <td>{{ new Date(activity.started).toLocaleString() }}</td>
          <td><a :href="`/users/${activity.userId}`">User {{ activity.userId }}</a></td>
        </tr>
      </tbody>
    </table>
  </app-layout>
</template>

<script>
app.component("activity-overview", {
  template: "#activity-overview",
  data: () => ({
    activities: [],
  }),
  created() {
    this.fetchActivities();
  },
  methods: {
    fetchActivities: function () {
      axios.get("/api/activities")
          .then(res => this.activities = res.data)
          .catch(error => {
            if (error.response && error.response.status === 404) {
              this.activities = [];
            } else {
              alert("Error while fetching activities");
            }
          });
    }
  }
});
</script>