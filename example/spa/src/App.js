import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

class App extends Component {
  constructor() {
    super();
    this.state = { message: "No server found!" };
  }

  componentDidMount() {
    fetch("/api/greetings")
      .then(result => result.json())
      .then(json => {
        this.setState({ message: json })
      })
      .catch(error => {}) // ignore error for now
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Welcome to React</h1>
        </header>
        <p className="App-intro">
          This is a message from server: <b>{ this.state.message }</b>
        </p>
      </div>
    );
  }
}

export default App;
