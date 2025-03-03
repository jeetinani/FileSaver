import { Route, Routes } from 'react-router-dom';
import './App.css';
import Upload from "./components/Upload";
import NavBar from "./components/NavBar";


function App() {
  return (
    <>
    <NavBar/>
    <div className="container">
      <Routes>
        <Route index path='/' element={<Upload />} />
        {/* <Route path='/' element={<Layout />}>
          <Route index path='/' element={<Upload />} />
        </Route> */}
      </Routes>
    </div>
    </>
  );
}

export default App;