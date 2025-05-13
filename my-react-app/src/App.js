import React, {useState} from 'react';
// import logo from './logo.svg';
import './App.css';
import {TypeInput} from './SelectInput.tsx';

// const options = [
//   { id: '1', displayName: 'Option 1' },
//   { id: '2', displayName: 'Option 2' },
//   { id: '3', displayName: 'Option 3' }
// ]

function App() {
  const [selected, setSelected] = useState('');

  const handleChange = (optionId) => {
    setSelected(optionId);
  }

  return (
    <div className="App">
      <header className="App-header">
        {/* <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a> */}
        
        <h1>Type a pokemon</h1>
        {/* <SelectInput id="fruit-select" options={options} onChange={handleChange} /> */}
        <TypeInput id="fruit-select" value={selected} onChange={handleChange}/>
        <p>You selected: {selected}</p>
      </header>
    </div>
  );
}

export default App;
