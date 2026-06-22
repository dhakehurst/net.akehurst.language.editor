<template>
  <img id='logo' alt="Vue logo" src="./assets/logo.png">
  <h1>AGL demo with Vue</h1>
  <div>
      <agl-editor-ace></agl-editor-ace>
      <agl-editor-ace />

  </div>
</template>


<script>
import AglEditorAceVue from './components/AglEditorAceVue.vue'

export default {
  name: 'App',
  components: {
    'agl-editor-ace' : AglEditorAceVue
  }
}
</script>



<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
#logo {
    width: 3em;
}
</style>
