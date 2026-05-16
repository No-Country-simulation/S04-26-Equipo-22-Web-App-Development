import { BrowserRouter, Routes, Route } from "react-router-dom";
import ChannelPreview from "../views/ChannelPreview";
import WeeklyDigestDetail from "../views/WeeklyDigestDetail";

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<h1>Home</h1>} />
        <Route path="/preview" element={<ChannelPreview />} />
        <Route path="/digest/:id" element={<WeeklyDigestDetail />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRouter;