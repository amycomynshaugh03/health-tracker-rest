<template id="user-profile">
  <app-layout>
    <div>
      <form v-if="user">
        <label class="col-form-label">User ID: </label>
        <input class="form-control" v-model="user.id" name="id" type="number" readonly/><br>
        <label class="col-form-label">Name: </label>
        <input class="form-control" v-model="user.name" name="username" type="text"/><br>
        <label class="col-form-label">Email: </label>
        <input class="form-control" v-model="user.email" name="email" type="email"/><br>
      </form>
      <dt v-if="user">
        <br>
        <a :href="`/users/${user.id}/activities`">View User Activities</a>
      </dt>
    </div>
  </app-layout>
</template>

<script>
app.component("user-profile", {
  template: "#user-profile",
  data: () => ({
    user: null
  }),
  created: function () {
    const userId = this.$javalin.pathParams["user-id"];
    const url = `/api/users/${userId}`
    axios.get(url)
        .then(res => this.user = res.data)
        .catch(() => alert("Error while fetching user" + userId));
  }
});
</script>