<template id="home-page">
  <app-layout>
      <div class="row">
        <div class="col">
          <div class="card">
            <h5 class="card-header">Registered Users</h5>
            <div class="card-body">
              <h5 class="card-title">{{users.length}} users</h5>
              <a href="/users" class="btn btn-primary">More Details...</a>
            </div>
          </div>
        </div>
        <div class="col">
          <div class="card">
            <h5 class="card-header">Total Activities</h5>
            <div class="card-body">
              <h5 class="card-title">{{activities.length}} activities</h5>
              <a href="/activities" class="btn btn-primary">More Details...</a>
            </div>
          </div>
        </div>
      </div>

  </app-layout>
</template>

<script>
app.component('home-page',
    {
      template: "#home-page",
      data: () => ({
        users: [],
        activities: []
      }),
      created() {
        axios.get("/api/users")
            .then(res => this.users = res.data)
            .catch(() => alert("Error while fetching users"));
        axios.get("/api/activities")
            .then(res => this.activities = res.data)
            .catch(() => alert("Error while fetching activities"));
      }
    });
</script>
