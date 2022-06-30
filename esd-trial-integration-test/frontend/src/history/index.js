import React from "react";
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@material-ui/core";
import data from "./data";
import moment from "moment";

export default function History() {
  const rows = data.list;

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Time</TableCell>
              <TableCell>m0</TableCell>
              <TableCell>m1</TableCell>
              <TableCell>activePower</TableCell>
              <TableCell>base</TableCell>
              <TableCell>target</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow>
                <TableCell>
                  {moment(row.time).format("YYYY-MM-DD HH:mm:ss")}
                </TableCell>
                <TableCell>{row.m0}</TableCell>
                <TableCell>{row.m1}</TableCell>
                <TableCell>{row.acPower}</TableCell>
                <TableCell>{row.base}</TableCell>
                <TableCell>{row.target}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}
