import React from "react";
import {
  Button,
  TextField,
  Radio,
  RadioGroup,
  FormControl,
  FormControlLabel,
  FormLabel,
} from "@material-ui/core";

import { useDispatch, useSelector } from "react-redux";
import {
  updateEsdServerUrl,
  updateTgId,
  updateQseId,
  updateServiceType,
} from "./reducer";
import { getValueFromEvent } from "../utils/viewHelper";

const updateConfig = (config) => {
  console.log("update config: ", config);

  const { esdServerUrl, tgId, qseId, serviceType } = config;

  sessionStorage.setItem("esdServerUrl", esdServerUrl);
  sessionStorage.setItem("tgId", tgId);
  sessionStorage.setItem("qseId", qseId);
  sessionStorage.setItem("serviceType", serviceType);
};

export default function Main() {
  const config = useSelector((state) => state.main.config);
  const dispatch = useDispatch();

  return (
    <div>
      <p>
        <TextField
          label="ESD Server URL"
          value={config.esdServerUrl}
          onChange={(event) =>
            dispatch(
              updateEsdServerUrl({
                esdServerUrl: getValueFromEvent(event),
              })
            )
          }
        />
      </p>

      <p>
        <TextField
          label="TgID"
          value={config.tgId}
          onChange={(event) =>
            dispatch(
              updateTgId({
                tgId: getValueFromEvent(event),
              })
            )
          }
        />
      </p>

      <p>
        <TextField
          label="QseID"
          value={config.qseId}
          onChange={(event) =>
            dispatch(
              updateQseId({
                qseId: getValueFromEvent(event),
              })
            )
          }
        />
      </p>

      <p>
        <FormControl component="serviceTypeSet">
          <FormLabel component="legend">ServiceType</FormLabel>
          <RadioGroup
            aria-label="serviceType"
            name="serviceTypes"
            value={config.serviceType.toString()}
            onChange={(event) =>
              dispatch(
                updateServiceType({
                  serviceType: parseInt(getValueFromEvent(event)),
                })
              )
            }
          >
            <FormControlLabel value="4" control={<Radio />} label="SR" />
            <FormControlLabel value="5" control={<Radio />} label="SUP" />
          </RadioGroup>
        </FormControl>
      </p>

      <Button
        id="update-config"
        variant="contained"
        onClick={() => updateConfig(config)}
      >
        Update
      </Button>
    </div>
  );
}
