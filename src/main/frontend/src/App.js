import { Route, Routes } from 'react-router-dom';
import './App.css';
import Layout from "./components/Layout";
import Upload from "./components/Upload";
import Retreive from "./components/Retreive";


function App() {
  return (
    <Routes>
      <Route path='/' element={<Layout />}>
        <Route index path='/' element={<Upload />} />
        {/* <Route path='/:uuid' element={<Retreive/>} /> */}
      </Route>
    </Routes>
  );
}

export default App;