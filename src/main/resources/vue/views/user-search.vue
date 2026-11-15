<template id="user-search">
  <app-layout>
    <h2>Find a User by Email</h2>
    <form class="row g-2 mt-2" @submit.prevent="searchUser">
      <div class="col-auto">
        <input class="form-control" v-model="email" type="email" placeholder="Enter email" required/>
      </div>
      <div class="col-auto">
        <button class="btn btn-primary" type="submit">Search</button>
      </div>
    </form>

    <div v-if="user" class="card mt-4">
      <h5 class="card-header">User Found</h5>
      <div class="card-body">
        <p><strong>ID:</strong> {{ user.id }}</p>
        <p><strong>Name:</strong> {{ user.name }}</p>
        <p><strong>Email:</strong> {{ user.email }}</p>
        <a :href="`/users/${user.id}`" class="btn btn-primary">View Profile</a>
      </div>
    </div>

    <div v-if="notFound" class="alert alert-warning mt-4">
      No user found with email {{ searchedEmail }}.
    </div>
  </app-layout>
</template>

<script>
app.component("user-search", {
  template: "#user-search",
  data: () => ({
    email: "",
    searchedEmail: "",
    user: null,
    notFound: false
  }),
  methods: {
    searchUser() {
      this.user = null;
      this.notFound = false;
      this.searchedEmail = this.email;
      axios.get(`/api/users/email/${encodeURIComponent(this.email)}`)
          .then(res => this.user = res.data)
          .catch(() => this.notFound = true);
    }
  }
});
</script>