import axios, { HttpStatusCode } from "axios";
import { saveAs } from "file-saver";
import { useState } from "react";

export default function Retreive({ uuid }) {

    const [alert, setAlert] = useState("");

    const downloadHandler = (e) => {

        e.preventDefault();
        axios.get(`/retrieve/${uuid}?passcode=` + e.target.passcode.value, {
            responseType: 'blob'
        }).then(response => {
            const contentDisposition = response.headers['content-disposition'];
            const filename = contentDisposition
                ? contentDisposition.split("filename=")[1]?.replace(/"/g, "")
                : "downloaded_file";

            saveAs(response.data, filename);
        })
            .catch(error => {
                console.error("Login failed:", error);
                if (error.response.status === HttpStatusCode.BadRequest) {
                    setAlert("Invalid Passcode");
                } else if (error.response.status === HttpStatusCode.NotFound) {
                    setAlert("File not found");
                } else {
                    setAlert("download failed");
                }
            });
    };

    return (
        <>
            <div>
                <div className="row">
                    <div className="col:6 mx-auto">
                        <h3>Download</h3>
                        <form onSubmit={downloadHandler}>

                            <div className="mb-3">
                                <label htmlFor="passcode" className="form-label">Passcode</label>
                                <input type="text" className="form-control" id="passcode"
                                    name="passcode" required placeholder="Passcode" />
                            </div>
                            <div className="mb-3">
                                <button type="submit" className="btn btn-success" name="upload">Download</button>
                            </div>
                            <div>
                                {alert}
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </>
    )
}