<template id="user-overview">
  <app-layout>
    <div>
      <ul class="user-overview-list">
        <li v-for="user in users">
          <a :href="`/users/${user.id}`">{{user.name}} ({{user.email}})</a>
        </li>
      </ul>
    </div>
  </app-layout>
</template>
<script>
app.component("user-overview", {
  template: "#user-overview",
  data: () => ({
    users: [],
  }),
  created() {
    this.fetchUsers();
  },
  methods: {
    fetchUsers: function () {
      axios.get("/api/users")
          .then(res => this.users = res.data)
          .catch(() => alert("Error while fetching users"));
    }
  }
});
</script>