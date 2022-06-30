import React from "react";
import {BrowserRouter as Router, Link, Route, Switch} from "react-router-dom";

import Main from "./main";
import Notice from "./notice";
import Alert from "./alert";
import History from "./history";

export default function App() {
    return (
        <Router>
            <div>
                <ul>
                    <li>
                        <Link to="/">主畫面</Link>
                    </li>
                    <li>
                        <Link to="/notice">調度通知</Link>
                    </li>
                    <li>
                        <Link to="/alert">告警</Link>
                    </li>
                    <li>
                        <Link to="/history">監控歷史</Link>
                    </li>
                </ul>

                <hr/>

                <Switch>
                    <Route exact path="/">
                        <Main/>
                    </Route>
                    <Route path="/notice">
                        <Notice/>
                    </Route>
                    <Route path="/alert">
                        <Alert/>
                    </Route>
                    <Route path="/history">
                        <History/>
                    </Route>
                </Switch>
            </div>
        </Router>
    );
}
