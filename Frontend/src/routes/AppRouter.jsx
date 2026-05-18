import { BrowserRouter, Routes, Route } from "react-router-dom";
import ChannelPreview from "../views/ChannelPreview";
import WeeklyDigestDetail from "../views/WeeklyDigestDetail";
import Settings from "../views/Settings";

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<h1>Home</h1>} />
        <Route path="/preview" element={<ChannelPreview />} />
        <Route path="/digest/:id" element={<WeeklyDigestDetail />} />
        <Route path="/settings" element={<Settings />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRouter;