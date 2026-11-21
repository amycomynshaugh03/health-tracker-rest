<template id="user-profile">
  <app-layout>
    <div v-if="noUserFound">
      <p> We're sorry, we were not able to retrieve this user.</p>
      <p> View <a :href="'/users'">all users</a>.</p>
    </div>
    <div class="card bg-light mb-3" v-if="user">
      <div class="card-header">
        <div class="row">
          <div class="col-6"> User Profile </div>
          <div class="col text-end">
            <button rel="tooltip" title="Update"
                class="btn btn-info btn-simple btn-link"
                @click="updateUser()">
                <i class="far fa-save" aria-hidden="true"></i>
            </button>
            <button rel="tooltip" title="Delete"
                class="btn btn-info btn-simple btn-link"
                @click="deleteUser()">
                <i class="fas fa-trash" aria-hidden="true"></i>
            </button>
          </div>
        </div>
      </div>
      <div class="card-body">
        <form>
          <div class="input-group mb-3">
            <span class="input-group-text" id="input-user-id">User ID</span>
            <input type="number" class="form-control" v-model="user.id" name="id" readonly placeholder="Id"/>
          </div>
          <div class="input-group mb-3">
            <span class="input-group-text" id="input-user-name">Name</span>
            <input type="text" class="form-control" v-model="user.name" name="name" placeholder="Name"/>
          </div>
          <div class="input-group mb-3">
            <span class="input-group-text" id="input-user-email">Email</span>
            <input type="email" class="form-control" v-model="user.email" name="email" placeholder="Email"/>
          </div>
        </form>
      </div>
      <div class="card-footer text-start">
        <p v-if="activities.length === 0"> No activities yet...</p>
        <p v-if="activities.length > 0"> Activities so far...</p>
        <ul>
          <li v-for="activity in activities" :key="activity.id">
            {{ activity.description }} for {{ activity.duration }} minutes
          </li>
        </ul>
      </div>
    </div>
  </app-layout>
</template>

<script>
app.component("user-profile", {
  template: "#user-profile",
  data: () => ({
    user: null,
    noUserFound: false,
    activities: [],
  }),
  created: function () {
    const userId = this.$javalin.pathParams["user-id"];
    const url = `/api/users/${userId}`
    axios.get(url)
        .then(res => this.user = res.data)
        .catch(error => {
          console.log("No user found for id passed in the path parameter: " + error)
          this.noUserFound = true
        })
    axios.get(url + `/activities`)
        .then(res => this.activities = res.data)
        .catch(error => {
          console.log("No activities added yet (this is ok): " + error)
        })
  },
  methods: {
    updateUser: function () {
      const userId = this.$javalin.pathParams["user-id"];
      const url = `/api/users/${userId}`
      axios.patch(url,
          {
            name: this.user.name,
            email: this.user.email
          })
          .then(() => alert("User updated!"))
          .catch(error => {
            console.log(error)
            alert("Error updating user")
          })
    },
    deleteUser: function () {
      if (confirm("Do you really want to delete?")) {
        const userId = this.$javalin.pathParams["user-id"];
        const url = `/api/users/${userId}`
        axios.delete(url)
            .then(() => {
              alert("User deleted")
              window.location.href = '/users';
            })
            .catch(error => {
              console.log(error)
              alert("Error deleting user")
            });
      }
    }
  }
});
</script>