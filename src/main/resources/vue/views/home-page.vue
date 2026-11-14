<!-- the "home-page" element is passed as a parameter to VueComponent in the ServerConfig file -->
<template id="home-page">
    <h1> Welcome to Health Tracker App</h1>
</template>

<script>
    app.component('home-page',
        {
            template: "#home-page",
        });
</script>
