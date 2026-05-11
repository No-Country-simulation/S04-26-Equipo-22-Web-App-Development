import { BrowserRouter, Routes, Route } from "react-router-dom";
import ChannelPreview from "../views/ChannelPreview";

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<h1>Home</h1>} />

        <Route
          path="/preview"
          element={<ChannelPreview />}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRouter;