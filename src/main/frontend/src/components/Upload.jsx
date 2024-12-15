import axios, { HttpStatusCode } from "axios";
import { useState } from "react";
import { useLocation } from "react-router-dom";
import Retreive from "./Retreive";



export default function Upload() {

    const [alert, setAlert] = useState("");
    const [downloadLink, setDownloadLink] = useState("");


    function useQuery() {
        return new URLSearchParams(useLocation().search);
    }
    const query = useQuery();
    const uuid = query.get("uuid");



    const uploadHandler = (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        axios.post("/upload", formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        })
            .then(resp => {
                setAlert("");
                setDownloadLink(<><p>Download link</p><a href={resp.data.retrievePath} target="_blank" rel="noopener noreferrer">{resp.data.retrievePath}</a></>);
                            
            }).catch((error) => {
                setDownloadLink("");
                console.error("Login failed:", error);
                if (error.response.status === HttpStatusCode.BadRequest) {
                    setAlert("File too large");
                } else {
                    setAlert("Failed");
                }
            });
    }
    return (
        <>
            {uuid ? <Retreive uuid={uuid} /> :
                <div className="row">
                    <div className="col:6 mx-auto">
                        <h3>Upload</h3>
                        <h6>Max file size limit is 10MB</h6>
                        <form onSubmit={uploadHandler}>
                            <div className="mb-3">
                                <label htmlFor="file" className="form-label">File</label>
                                <input type="file" className="form-control" id="file"
                                    name="file" required placeholder="File" />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="passcode" className="form-label">Passcode</label>
                                <input type="text" className="form-control" id="passcode"
                                    name="passcode" required placeholder="Passcode" />
                            </div>
                            <div className="mb-3">
                                <button type="submit" className="btn btn-success" name="upload">Upload</button>
                            </div>
                            <div>
                                {alert}
                            </div>
                            <div>
                            {downloadLink}
                            </div>
                        </form>
                    </div>
                </div>
            }
        </>
    )
}